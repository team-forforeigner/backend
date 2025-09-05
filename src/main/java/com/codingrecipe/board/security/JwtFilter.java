package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.LoggedOutTokenRepository;
import com.codingrecipe.board.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final LoggedOutTokenRepository loggedOutTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        try {
            token = authorization.split(" ")[1];
        } catch (Exception e) {
            log.error("토큰 추출 중 오류 발생: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (loggedOutTokenRepository.existsByToken(token)) {
            log.warn("이미 로그아웃 처리된 토큰입니다.");
            // [수정] sendError 대신 직접 JSON 응답을 생성하는 헬퍼 메소드 호출
            setErrorResponse(response, ErrorCode.UNAUTHORIZED_USER, "Logged out token");
            return;
        }

        try {
            String email = jwtUtil.getEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<Member> memberOpt = memberRepository.findByEmail(email);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();

                    if (member.getStatus() == MemberStatus.SUSPENDED) {
                        log.warn("정지된 계정의 토큰입니다: {}", email);
                        // sendError 대신 직접 JSON 응답을 생성하는 헬퍼 메소드 호출
                        setErrorResponse(response, ErrorCode.USER_SUSPENDED);
                        return;
                    }

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey())));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("토큰이 만료되었습니다: {}", e.getMessage());
            // 만료된 토큰의 경우, 클라이언트가 재로그인해야 하므로 401 Unauthorized 에러를 응답
            setErrorResponse(response, ErrorCode.UNAUTHORIZED_USER, "Token expired");
            return;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            setErrorResponse(response, ErrorCode.UNAUTHORIZED_USER, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * [추가] 에러 응답을 ApiResponseDto JSON 형태로 직접 생성하여 반환하는 헬퍼 메소드
     */
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        setErrorResponse(response, errorCode, errorCode.getMessage());
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());
        // ApiResponseDto를 JSON 문자열로 변환하여 응답 본문에 작성
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponseDto.error(errorCode, message)));
    }
}
