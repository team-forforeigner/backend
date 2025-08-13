package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceRequestDTO {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("choiceId")
    private Long choiceId;
}
