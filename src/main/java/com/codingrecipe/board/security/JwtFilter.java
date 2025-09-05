package com.codingrecipe.board.security;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.LoggedOutTokenRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
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
            setErrorResponse(response, ErrorCode.UNAUTHORIZED_USER, "Logged out token");
            return;
        }

        try {
            if (!jwtUtil.isTokenExpired(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                // DB 조회 없이 토큰에서 직접 사용자 정보를 파싱합니다.
                UserPrincipal userPrincipal = jwtUtil.parseToken(token);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(errorCode.getStatus().value());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponseDto.error(errorCode, message)));
    }
}
