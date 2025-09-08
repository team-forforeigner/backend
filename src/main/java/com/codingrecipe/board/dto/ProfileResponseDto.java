package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileResponseDto {
    private final Long id;
    private final String email;
    private final String nickname;
    private final int level;
    private final int experience;
    private final String currentTitle;
    private final String currentBadge; // 현재 설정된 캐릭터 이미지 URL
    private final String profileImageUrl;
    private final String backgroundImageUrl;
    private final List<String> availableTitles; // 선택 가능한 모든 칭호 목록
    private final List<AvailableBadgeDto> availableBadges; // 선택 가능한 모든 캐릭터 목록

    public ProfileResponseDto(Member member, List<String> availableTitles, List<AvailableBadgeDto> availableBadges) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.level = member.getLevel();
        this.experience = member.getExperience();
        this.currentTitle = member.getTitle();
        this.currentBadge = member.getBadge();
        this.profileImageUrl = member.getProfileImageUrl();
        this.backgroundImageUrl = member.getBackgroundImageUrl();
        this.availableTitles = availableTitles;
        this.availableBadges = availableBadges;
    }
}
