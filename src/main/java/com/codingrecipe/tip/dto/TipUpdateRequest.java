package com.codingrecipe.tip.dto;

import com.codingrecipe.tip.TipCategory;
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

    @NotNull
    private Long id;
    @NotNull
    private String question;
    @NotNull
    private String answer;
    private String source;     // 선택
    @NotNull
    private TipCategory category;

}
