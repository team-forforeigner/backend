package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesItemDTO {
    @JsonProperty("seriesId")
    private Long seriesId;

    @JsonProperty("title")
    private String title;
}
