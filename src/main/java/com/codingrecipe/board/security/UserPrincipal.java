package com.codingrecipe.board.security;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security의 인증 Principal로 사용될 클래스.
 * JWT 토큰에 담길 사용자 정보를 캡슐화하고, UserDetails 인터페이스를 구현하여 Security와 호환됩니다.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String nickname, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.authorities = authorities;
    }

    /**
     * Member 엔티티를 받아서 UserPrincipal 객체를 생성하는 정적 팩토리 메소드
     */
    public static UserPrincipal from(Member member) {
        return new UserPrincipal(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                Collections.singletonList(new SimpleGrantedAuthority(member.getRoleKey()))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // JWT 기반이므로 비밀번호는 사용하지 않음
        return null;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username은 고유 식별자를 의미하므로, 이메일을 반환
        return email;
    }

    // 계정 상태 관련 메소드들 (JWT 자체의 만료 시간으로 관리되므로 모두 true 반환)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
