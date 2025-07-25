package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipDTO {

    private Long id;
    private String question;
    private String answer;
    private String source;
    private TipCategory category;

    // TipEntity -> TipDTO 변환
    public TipEntity toEntity() {
        return TipEntity.builder()
                .id(this.id)
                .question(this.question)
                .answer(this.answer)
                .source(this.source)
                .category(this.category)
                .build();
    }

    // TipDTO -> TipEntity 변환
    public static TipDTO fromEntity(TipEntity entity) {
        return TipDTO.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .source(entity.getSource())
                .category(entity.getCategory())
                .build();
    }

}
