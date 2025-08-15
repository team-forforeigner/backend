package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevelDTO {
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("completedSeriesCount")
    private int completedSeriesCount;

    @JsonProperty("levelName")
    private String levelName;
}
