package com.survival.DTO;

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

    @JsonProperty("choiceId")
    private Long choiceId;

    @JsonProperty("timestamp")
    private String timestamp;
}
