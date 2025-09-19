package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.Member;
import lombok.Getter;

@Getter
public class PublicProfileResponseDto {
    private final Long id;
    private final String nickname;
    private final int level;
    private final String currentTitle;
    private final String currentBadge;
    private final String profileImageUrl;
    private final String backgroundImageUrl;

    public PublicProfileResponseDto(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.level = member.getLevel();
        this.currentTitle = member.getTitle();
        this.currentBadge = member.getBadge();
        this.profileImageUrl = member.getProfileImageUrl();
        this.backgroundImageUrl = member.getBackgroundImageUrl();
    }
}
