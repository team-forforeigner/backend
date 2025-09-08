package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.AvailableBadgeDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TitleAndBadgeManager {

    // 레벨별 칭호 정보 (Key: 최소 레벨, Value: 칭호 이름)
    private static final Map<Integer, String> TITLE_MAP = Map.of(
            1, "아기 까치",
            5, "한반도 거북이",
            10, "구미호",
            15, "100일동안 마늘만 먹고 지낸 곰",
            20, "한반도 수호자 호랑이",
            25, "백호"
    );

    // 레벨별 캐릭터 정보
    private static final Map<Integer, AvailableBadgeDto> BADGE_MAP = Map.of(
            1, new AvailableBadgeDto("아기 까치", "https://your-s3-bucket/badges/magpie.png"),
            5, new AvailableBadgeDto("한반도 거북이", "https://your-s3-bucket/badges/turtle.png"),
            10, new AvailableBadgeDto("구미호", "https://your-s3-bucket/badges/gumiho.png"),
            15, new AvailableBadgeDto("100일동안 마늘만 먹고 지낸 곰", "https://your-s3-bucket/badges/legend_bear.png"),
            20, new AvailableBadgeDto("한반도 수호자 호랑이", "https://your-s3-bucket/badges/guardian_tiger.png"),
            25, new AvailableBadgeDto("백호", "https://your-s3-bucket/badges/white_tiger.png")
    );


    /**
     * 주어진 레벨에서 획득 가능한 모든 칭호 목록을 반환합니다.
     */
    public List<String> getAvailableTitles(int userLevel) {
        List<String> titles = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : TITLE_MAP.entrySet()) {
            if (userLevel >= entry.getKey()) {
                titles.add(entry.getValue());
            }
        }
        return titles;
    }

    /**
     * 주어진 레벨에서 획득 가능한 모든 배지(캐릭터) 목록을 반환합니다.
     */
    public List<AvailableBadgeDto> getAvailableBadges(int userLevel) {
        List<AvailableBadgeDto> badges = new ArrayList<>();
        for (Map.Entry<Integer, AvailableBadgeDto> entry : BADGE_MAP.entrySet()) {
            if (userLevel >= entry.getKey()) {
                badges.add(entry.getValue());
            }
        }
        return badges;
    }
}
