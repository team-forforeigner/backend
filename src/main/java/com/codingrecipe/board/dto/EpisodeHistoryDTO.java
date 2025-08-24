package com.codingrecipe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

// 유저 선택 기록 (조회용)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpisodeHistoryDTO {
    @JsonProperty("episodeId")
    private Long episodeId;

    @JsonProperty("choiceDescription")
    private String choiceDescription;

    @JsonProperty("timestamp")
    private String timestamp;
}
