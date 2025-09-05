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

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Value("${oauth.redirect-url}")
    private String redirectUrl;

    /**
     * OAuth2 인증 성공 시 호출되는 메서드
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 로그인 성공!");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getName();

        // 토큰에 모든 정보를 담기 위해 DB에서 Member 객체를 조회합니다. (로그인 시 딱 한 번만 실행)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Member 객체 자체를 넘겨서 토큰을 생성하도록 변경
        String token = jwtUtil.generateToken(member);
        log.info("발급된 JWT 토큰 (Role: {}): {}", member.getRoleKey(), token);

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("token", token)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

