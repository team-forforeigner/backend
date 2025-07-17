package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공!");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String userId = oAuth2User.getName();

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth2 인증 정보에 해당하는 사용자가 없습니다."));

        String token = jwtUtil.generateToken(member.getUserId());
        log.info("발급된 JWT 토큰: {}", token);

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("token", token)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}