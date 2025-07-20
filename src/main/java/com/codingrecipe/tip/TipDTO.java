package com.codingrecipe.tip;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TipDTO {

    private String question;
    private String answer;
    private String source;
    private TipCategory category;

    public TipEntity toEntity() {
        return TipEntity.builder()
                .question(this.question)
                .answer(this.answer)
                .source(this.source)
                .category(this.category)
                .build();
    }

}
