package com.survival.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SurvivalResultDTO {
    //응답 DTO
    private Long nextEpisodeId;
    private String message;
}
