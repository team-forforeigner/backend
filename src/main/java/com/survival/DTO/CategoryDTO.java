package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("title")
    private Long title;

    @JsonProperty("description")
    private Long description;
}
