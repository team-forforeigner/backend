package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipResponse {

    @NotNull
    private Long id;
    @NotNull
    private String question;
    @NotNull
    private String answer;
    private String source;  // 선택
    @NotNull
    private TipCategory category;

    // DTO -> Entity 변환
    public TipEntity toEntity() {
        return TipEntity.builder()
                .id(this.id)
                .question(this.question)
                .answer(this.answer)
                .source(this.source)
                .category(this.category)
                .build();
    }

    // Entity -> DTO 변환
    public static TipResponse fromEntity(TipEntity entity) {
        return TipResponse.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .answer(entity.getAnswer())
                .source(entity.getSource())
                .category(entity.getCategory())
                .build();
    }

}
