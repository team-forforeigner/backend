// 사용자 프로필 정보를 응답으로 보낼 때 사용하는 DTO
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Long memberId; // 사용자 고유 식별자
    private final String nickname; // 닉네임
    private final int level; // 레벨
    private final int experience; // 경험치
    private final String title; // 설정된 칭호
    private final int playCount; // 퀴즈 참여 횟수
    private final String badge; // 획득한 배지
    private final String profileImageUrl; // 프로필 이미지 URL
    private final String backgroundImageUrl; // 프로필 배경 이미지 URL

    //  Member 엔티티를 받아 UserProfileResponse DTO를 생성
    public UserProfileResponse(Member member) {
        this.memberId = member.getId();
        this.nickname = member.getNickname();
        this.level = member.getLevel();
        this.experience = member.getExperience();
        this.title = member.getTitle();
        this.playCount = member.getPlayCount();
        this.badge = member.getBadge();
        this.profileImageUrl = member.getProfileImageUrl();
        this.backgroundImageUrl = member.getBackgroundImageUrl();
    }
}
