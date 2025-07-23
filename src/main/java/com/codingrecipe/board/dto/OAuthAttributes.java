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
    private final String nickname; // 변경: name -> nickname
    private final String email;
    private final String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String nickname, String email, String picture) { // 변경: name -> nickname, userId 삭제
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.nickname = nickname; // 변경
        this.email = email;
        this.picture = picture;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle(userNameAttributeName, attributes);
        }
        // 추후 다른 소셜 로그인(네이버, 카카오 등)을 추가할 수 있습니다.
        throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        // 구글 로그인의 경우, nameAttributeKey의 기본값은 'sub' 입니다.
        // 우리는 이메일을 기준으로 사용자를 식별하므로, nameAttributeKey를 'email'로 사용하도록 설정합니다.
        return OAuthAttributes.builder()
                .nickname((String) attributes.get("name")) // 변경
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey("email") // 변경: Principal의 이름을 email로 사용하도록 키 값 변경
                .build();
    }

    // 처음 가입하는 사용자일 경우, Member 엔티티를 생성하는 메소드
    public Member toEntity(String registrationId) {
        return Member.builder()
                .nickname(nickname) // 변경: name -> nickname
                .email(email)
                .picture(picture)
                .role(Role.USER) // 기본 권한
                .provider(registrationId) // 소셜 로그인 제공자 (e.g., "google")
                .providerId(String.valueOf(attributes.get("sub"))) // Google의 사용자 고유 ID
                .emailVerified(true) // 소셜 로그인은 이메일이 인증된 것으로 간주
                .build();
    }
}