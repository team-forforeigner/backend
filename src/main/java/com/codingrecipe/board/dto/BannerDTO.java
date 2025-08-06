// 배너 데이터 전송을 위한 DTO 클래스
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.BannerEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BannerDTO {
    private Long id; // 배너 고유 식별자
    private String title; // 배너 제목
    private String description; // 배너 설명
    private String imageUrl; // 배너 이미지 URL
    private String linkUrl; // 배너 클릭 시 이동할 링크 URL
    private boolean isExposed; // 배너 노출 여부
    private int displayOrder; // 배너 표시 순서

    // BannerEntity 객체를 BannerDTO 객체로 변환하는 정적 팩토리 메서드(주로 DB 조회 결과를 클라이언트에게 응답으로 보낼 때 사용
    public static BannerDTO fromEntity(BannerEntity entity) {
        BannerDTO dto = new BannerDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setImageUrl(entity.getImageUrl());
        dto.setLinkUrl(entity.getLinkUrl());
        dto.setExposed(entity.isExposed());
        dto.setDisplayOrder(entity.getDisplayOrder());
        return dto;
    }
}
