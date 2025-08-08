package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.TipCategory;
import com.codingrecipe.board.domain.TipEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipCreateRequest implements TipRequestBase {

    @NotBlank(message = "질문은 필수입니다.")
    private String question;
    @NotBlank(message = "답변은 필수입니다.")
    private String answer;
    private String source;  // 선택
    @NotNull(message = "카테고리는 필수입니다.")
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
