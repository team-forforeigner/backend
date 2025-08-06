package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.domain.TipCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipUpdateRequest implements TipRequestBase {

    @NotBlank(message = "질문은 필수입니다.")
    private String question;
    @NotBlank(message = "답변은 필수입니다.")
    private String answer;
    private String source;     // 선택
    @NotNull(message = "카테고리는 필수입니다.")
    private TipCategory category;

}
