package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.BannerEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BannerDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String linkUrl;
    private boolean isExposed;
    private int displayOrder;

    // Entity를 DTO로 변환하는 정적 메소드 (목록 조회 등 응답에 사용)
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