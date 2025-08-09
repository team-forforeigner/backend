// 모든 API 요청에 대해 JWT 토큰의 유효성을 검증하는 필터
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
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 요청이 들어올 때마다 한 번씩 실행되는 필터의 핵심 로직
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에서 Authorization 헤더를 가져옴
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 토큰 검증을 건너뜀
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token;
        try {
            // "Bearer " 부분을 제외하고 실제 토큰만 추출
            token = authorization.split(" ")[1];
        } catch (Exception e) {
            log.error("토큰 추출 중 오류 발생", e);
            filterChain.doFilter(request, response);
            return;
        }

        // ---  로그아웃된 토큰인지 확인 ---
        String isLogout = redisTemplate.opsForValue().get(token);
        if (isLogout != null && isLogout.equals("logout")) {
            log.warn("이미 로그아웃된 토큰입니다.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 만료 여부 확인
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("토큰이 만료되었습니다");
                filterChain.doFilter(request, response);
                return;
            }

            // 토큰에서 사용자 이메일 추출
            String email = jwtUtil.getEmail(token);

            // 이메일이 존재하고, 현재 SecurityContext에 인증 정보가 없는 경우
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // DB에서 해당 이메일의 사용자 정보를 조회
                Optional<Member> memberOpt = memberRepository.findByEmail(email);

                if (memberOpt.isPresent()) {
                    Member member = memberOpt.get();
                    // Spring Security용 인증 토큰 생성
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(member.getEmail(), null, Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey())));
                    // 요청에 대한 상세 정보 설정
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // SecurityContext에 인증 정보 저장
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }

        } catch (ExpiredJwtException e) {
            log.error("토큰 파싱 오류: 토큰이 만료되었습니다", e);
        } catch (Exception e) {
            log.error("토큰 파싱 오류: 유효하지 않은 토큰입니다", e);
        }

        // 다음 필터로 요청과 응답을 전달
        filterChain.doFilter(request, response);
    }
}
