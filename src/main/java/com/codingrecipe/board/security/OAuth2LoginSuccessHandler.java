// 소셜 로그인(OAuth2) 성공 후의 로직을 처리하는 핸들러
package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.redirect-url}")
    private String redirectUrl;

    /**
     * OAuth2 인증 성공 시 호출되는 메서드
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 로그인 성공!");
        // 인증 객체에서 OAuth2 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 사용자 정보에서 이메일 추출
        String email = oAuth2User.getName();

        // DB에서 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 추출한 이메일로 JWT 토큰 생성
        String token = jwtUtil.generateToken(email);
        log.info("발급된 JWT 토큰: {}", token);

        // 프론트엔드 리다이렉트 URL에 토큰을 쿼리 파라미터로 추가
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("token", token)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        // 생성된 URL로 클라이언트를 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
