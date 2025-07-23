package com.codingrecipe.tip;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class TipDTO {

    private String question;
    private String answer;
    private String source;
    private Set<TipCategory> categories;

    public TipEntity toEntity() {
        return TipEntity.builder()
                .question(this.question)
                .answer(this.answer)
                .source(this.source)
                .categories(this.categories) // 복수 카테고리
                .build();
    }

}
