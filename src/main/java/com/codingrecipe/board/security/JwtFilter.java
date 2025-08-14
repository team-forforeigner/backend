package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.repository.LoggedOutTokenRepository;
import com.codingrecipe.board.repository.MemberRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

        // ---  로그아웃된 토큰인지 DB에서 확인 ---
        if (loggedOutTokenRepository.existsByToken(token)) {
            log.warn("이미 로그아웃 처리된 토큰입니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is logged out");
            return;
        }

        try {
            // 토큰에서 사용자 이메일 추출
            String email = jwtUtil.getEmail(token);

            // 이메일이 존재하고, 현재 SecurityContext에 인증 정보가 없는 경우
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 토큰 만료 여부 확인 (DB 조회 전에 확인하여 불필요한 부하 감소)
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("토큰이 만료되었습니다.");
                    filterChain.doFilter(request, response);
                    return;
                }

                Optional<Member> memberOpt = memberRepository.findByEmail(email);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey())));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다: {}", e.getMessage());
        } catch (Exception e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
