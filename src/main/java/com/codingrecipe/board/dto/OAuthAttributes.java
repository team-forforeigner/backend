package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

public record OAuthAttributes(Map<String, Object> attributes,
                              String nameAttributeKey,
                              String nickname,
                              String email,
                              String picture) {

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(attributes);
        }
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
                attributes,
                "email",
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture")
        );
    }

    public Member toEntity(String registrationId) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .profileImageUrl(picture)
                .role(Role.USER)
                .provider(registrationId)
                .providerId(String.valueOf(attributes.get("sub")))
                .emailVerified(true)
                .build();
    }
}