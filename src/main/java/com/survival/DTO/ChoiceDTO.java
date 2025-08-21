package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceDTO {
    @JsonProperty("choiceId")
    private Long choiceId;

    @JsonProperty("description")
    private Map<String, String> description;

    @JsonProperty("nextEpisodeId")
    private Long nextEpisodeId;
}
