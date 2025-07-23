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

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        log.info("소셜 로그인 시작 >>> registrationId = {}, userNameAttributeName = {}", registrationId, userNameAttributeName);

        // OAuth2 사용자 정보 파싱
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        // 사용자 정보 저장 또는 업데이트
        Member member = saveOrUpdate(attributes, registrationId);

        // Spring Security의 Principal로 사용될 OAuth2User 객체 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey() // 변경: OAuthAttributes에서 nameAttributeKey를 가져옴 (주로 'email')
        );
    }

    // 소셜 로그인 사용자 정보가 DB에 있으면 업데이트, 없으면 새로 저장
    private Member saveOrUpdate(OAuthAttributes attributes, String registrationId) {
        Member member = memberRepository.findByEmail(attributes.getEmail())
                // DB에 이메일이 이미 있는 경우 -> 닉네임과 사진 정보 업데이트
                .map(entity -> entity.update(attributes.getNickname(), attributes.getPicture()))
                // DB에 이메일이 없는 경우 -> 새로운 Member 엔티티 생성
                .orElse(attributes.toEntity(registrationId));

        return memberRepository.save(member);
    }
}