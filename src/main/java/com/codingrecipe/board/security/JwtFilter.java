package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.MemberStatus;
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

        if (loggedOutTokenRepository.existsByToken(token)) {
            log.warn("이미 로그아웃 처리된 토큰입니다.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is logged out");
            return;
        }

        try {
            String email = jwtUtil.getEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isTokenExpired(token)) {
                    log.warn("토큰이 만료되었습니다.");
                    filterChain.doFilter(request, response);
                    return;
                }

                Optional<Member> memberOpt = memberRepository.findByEmail(email);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();

                    if (member.getStatus() == MemberStatus.SUSPENDED) {
                        log.warn("정지된 계정의 토큰입니다: {}", email);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Suspended account");
                        return;
                    }

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