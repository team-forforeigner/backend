// 소셜 로그인(OAuth2) 후 가져온 사용자 정보(이름, 이메일 등)를 처리하는 서비스
package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.OAuthAttributes;
import com.codingrecipe.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    /**
     * 소셜 로그인 제공자로부터 사용자 정보를 받아와 처리
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 사용하여 사용자 정보 로드
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스 구분 (예: "google", "kakao")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("소셜 로그인 시작 >>> registrationId = {}", registrationId);

        // 소셜 로그인 제공자별로 사용자 정보를 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());

        // 파싱된 정보를 바탕으로 DB에 사용자를 저장하거나 업데이트
        Member member = saveOrUpdate(attributes, registrationId);

        // Spring Security가 사용할 인증 객체를 생성하여 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                attributes.attributes(),
                attributes.nameAttributeKey()
        );
    }

    /**
     * 소셜 로그인 사용자가 DB에 있으면 업데이트, 없으면 새로 저장
     */
    private Member saveOrUpdate(OAuthAttributes attributes, String registrationId) {
        // 이메일로 기존 사용자인지 확인
        Member member = memberRepository.findByEmail(attributes.email())
                // 기존 사용자이면 이름과 프로필 사진 업데이트
                .map(entity -> entity.update(attributes.nickname(), attributes.picture()))
                // 신규 사용자이면 attributes를 바탕으로 Member 엔티티 생성
                .orElse(attributes.toEntity(registrationId));

        // DB에 저장 후 반환
        return memberRepository.save(member);
    }
}
