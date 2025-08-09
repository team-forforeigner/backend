// 소셜 로그인(OAuth2)을 통해 받은 사용자 정보를 담는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.domain.Role;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

/**
 * 소셜 로그인 제공자(Google 등)로부터 받은 사용자 속성을 처리하는 불변 데이터 객체
 * @param attributes 원본 사용자 정보 Map
 * @param nameAttributeKey 사용자 이름 속성의 키 값
 * @param nickname 사용자 닉네임
 * @param email 사용자 이메일
 * @param picture 사용자 프로필 사진 URL
 */
public record OAuthAttributes(Map<String, Object> attributes,
                              String nameAttributeKey,
                              String nickname,
                              String email,
                              String picture) {

    /**
     * 소셜 로그인 제공자에 따라 적절한 메서드를 호출하여 OAuthAttributes 객체를 생성
     */
    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(attributes);
        }
        // 지원하지 않는 소셜 로그인일 경우 예외 발생
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    /**
     * Google 로그인 응답 속성에서 정보를 추출하여 OAuthAttributes 객체를 생성
     */
    private static OAuthAttributes ofGoogle(Map<String, Object> attributes) {
        return new OAuthAttributes(
                attributes,
                "email", // Google에서는 email을 주요 식별자로 사용
                (String) attributes.get("name"),
                (String) attributes.get("email"),
                (String) attributes.get("picture")
        );
    }

    /**
     * OAuthAttributes 객체의 정보를 바탕으로 Member 엔티티를 생성
     * (소셜 로그인 사용자를 DB에 저장하기 위함)
     */
    public Member toEntity(String registrationId) {
        return Member.builder()
                .nickname(nickname)
                .email(email)
                .profileImageUrl(picture)
                .role(Role.USER) // 소셜 로그인 사용자는 기본적으로 USER 권한 부여
                .provider(registrationId) // 로그인 제공자 정보 저장
                .providerId(String.valueOf(attributes.get("sub"))) // Google의 고유 사용자 ID
                .emailVerified(true) // 소셜 로그인이므로 이메일은 인증된 것으로 간주
                .build();
    }
}
