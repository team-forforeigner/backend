package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.domain.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipCreateRequest implements TipRequestBase {

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
                .question(this.question)
                .answer(this.answer)
                .source(this.source)
                .category(this.category)
                .build();
    }

}
