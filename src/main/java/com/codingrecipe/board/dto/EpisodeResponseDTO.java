package com.codingrecipe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodeResponseDTO {
    @JsonProperty("episodeId")
    private Long episodeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private List<String> content;

    @JsonProperty("choices")
    private List<ChoiceDTO>  choices;
}