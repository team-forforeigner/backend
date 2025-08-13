package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.survival.Entity.EpisodesEntity;
import lombok.*;

import java.util.List;

//

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodeResponseDTO {
    @JsonProperty("epiosdeId")
    private Long episodeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("choices")
    private List<ChoiceDTO>  choices;

    public static EpisodeResponseDTO fromEntity(EpisodesEntity episode) {
        return null;
    }
}
