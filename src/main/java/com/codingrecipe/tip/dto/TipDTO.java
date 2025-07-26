package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipDTO {

    private Long id;

    @NotBlank(message = "질문은 필수 입력입니다.")
    private String question;

    @NotBlank(message = "답변은 필수 입력입니다.")
    private String answer;

    private String source;

    @NotBlank(message = "카테고리는 필수 입력입니다.")
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
