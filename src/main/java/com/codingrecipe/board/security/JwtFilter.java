package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
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
            log.error("토큰 추출 중 오류 발생", e);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("토큰이 만료되었습니다.");
                filterChain.doFilter(request, response);
                return;
            }

            // 변경: getUserId() -> getEmail()
            String email = jwtUtil.getEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 변경: findByUserId() -> findByEmail()
                Optional<Member> memberOpt = memberRepository.findByEmail(email);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    // 변경: Principal을 email로 설정
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey())));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        } catch (ExpiredJwtException e) {
            log.error("토큰 파싱 오류: 토큰이 만료되었습니다.", e);
        } catch (Exception e) {
            log.error("토큰 파싱 오류: 유효하지 않은 토큰입니다.", e);
        }

        filterChain.doFilter(request, response);
    }
}