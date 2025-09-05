package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    @Value("${oauth.redirect-url}")
    private String redirectUrl;

    /**
     * OAuth2 인증 성공 시 호출되는 메서드 (수정)
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("OAuth2 로그인 성공!");
        // 인증 객체에서 OAuth2 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 사용자 정보에서 이메일 추출
        String email = oAuth2User.getName();

        // Spring Security의 인증 정보에서 사용자의 Role을 동적으로 추출합니다.
        String roleKey = authentication.getAuthorities().stream()
                .findFirst() // 첫 번째 권한을 가져옴 (USER 또는 ADMIN)
                .map(GrantedAuthority::getAuthority)
                .orElse(Role.USER.getKey()); // 권한이 없는 경우 기본값으로 USER 설정

        Role role = Role.valueOf(roleKey.replace("ROLE_", ""));

        // 이메일과 동적으로 얻어온 역할을 함께 전달하여 토큰을 생성합니다.
        String token = jwtUtil.generateToken(email, role);
        log.info("발급된 JWT 토큰 (Role: {}): {}", role.name(), token);

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
