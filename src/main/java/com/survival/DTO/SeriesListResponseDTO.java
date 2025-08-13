package com.survival.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesListResponseDTO {
    @JsonProperty("categoryId")
    private Long categoryId;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("categoryDescription")
    private String categoryDescription;

    @JsonProperty("series")
    private List<SeriesItemDTO> series;
}