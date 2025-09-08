package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.AvailableBadgeDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class TitleAndBadgeManager {

    private final S3UploaderService s3UploaderService;

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
            1, new AvailableBadgeDto("아기 까치", "uploads/badges/magpie.png"),
            5, new AvailableBadgeDto("한반도 거북이", "uploads/badges/turtle.png"),
            10, new AvailableBadgeDto("구미호", "uploads/badges/gumiho.png"),
            15, new AvailableBadgeDto("100일동안 마늘만 먹고 지낸 곰", "uploads/badges/bear.png"),
            20, new AvailableBadgeDto("한반도 수호자 호랑이", "uploads/badges/tiger.png"),
            25, new AvailableBadgeDto("백호", "uploads/badges/WhiteTiger.png")
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
                // S3 Key -> Presigned URL로 변환
                String s3Key = entry.getValue().getImageUrl(); // 기존 DTO의 imageUrl에 S3 Key 저장
                String presignedUrl = s3UploaderService.generatePresignedUrl(s3Key);
                badges.add(new AvailableBadgeDto(entry.getValue().getName(), presignedUrl));
//                badges.add(entry.getValue());
            }
        }
        return badges;
    }
}
