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

        // ========================= [디버깅 로그] 1. 필터 시작 =========================
        log.info(">>> JWT 필터 시작: URI: {}", request.getRequestURI());

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        // ========================= [디버깅 로그] 2. 헤더 확인 =========================
        log.info("Authorization 헤더: {}", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Authorization 헤더가 없거나 'Bearer '로 시작하지 않습니다. 필터를 종료합니다.");
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        try {
            token = authorization.split(" ")[1];
            // ========================= [디버깅 로그] 3. 토큰 추출 =========================
            log.info("추출된 토큰: {}", token);
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

            String userId = jwtUtil.getUserId(token);
            // ========================= [디버깅 로그] 4. 토큰에서 UserId 추출 =========================
            log.info("토큰에서 추출한 userId: {}", userId);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // ========================= [디버깅 로그] 5. DB에서 사용자 정보 조회 =========================
                log.info("DB에서 사용자 [{}] 조회를 시작합니다.", userId);
                Optional<Member> memberOpt = memberRepository.findByUserId(userId);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    // ========================= [디버깅 로그] 6. 사용자 정보 조회 성공 =========================
                    log.info("사용자 [{}] 정보 조회 성공! 역할: {}", userId, member.getRoleKey());

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getUserId(), null, Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey())));
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    // ========================= [디버깅 로그] 7. 인증 성공 =========================
                    log.info(">>> 사용자 [{}] 인증 성공! SecurityContext에 저장되었습니다.", userId);
                } else {
                    // ========================= [디버깅 로그] 6-1. 사용자 정보 조회 실패 =========================
                    log.error("DB에 사용자 [{}] 정보가 없습니다.", userId);
                }
            }

        } catch (ExpiredJwtException e) {
            log.error("토큰 파싱 오류: 토큰이 만료되었습니다.", e);
        } catch (Exception e) {
            log.error("토큰 파싱 오류: 유효하지 않은 토큰입니다.", e);
        }

        filterChain.doFilter(request, response);
        log.info(">>> JWT 필터 종료: URI: {}", request.getRequestURI());
    }
}