package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class UserProfileResponse {

    private final Long memberId;
    private final String nickname;
    private final int level;
    private final int experience;
    private final String title;
    private final int playCount;
    private final String badge;
    private final String profileImageUrl;
    private final String backgroundImageUrl;

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