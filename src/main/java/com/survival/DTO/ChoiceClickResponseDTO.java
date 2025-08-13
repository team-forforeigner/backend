package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//프론트 페이지에서 사용자가 choice를 선택하면 다음 에피소드로 안내하기 위한
// entity들을 정리

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceClickResponseDTO {
    @JsonProperty("nextEpisodeId")
    private Long nextEpisodeId;

    @JsonProperty("message")
    private String message;
}
