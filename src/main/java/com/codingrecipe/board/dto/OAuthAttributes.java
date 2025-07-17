package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final String picture;
    private final String userId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture, String userId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.userId = userId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        String userId = "google_" + attributes.get(userNameAttributeName);
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .userId(userId)
                .attributes(attributes)
                .nameAttributeKey("userId")
                .build();
    }

    public Member toEntity(String registrationId) {
        return Member.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER)
                .provider(registrationId)
                .providerId(String.valueOf(attributes.get("sub"))) // Google의 고유 ID는 "sub"
                .userId(userId)
                .build();
    }
}