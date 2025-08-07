package com.codingrecipe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FinalResponseDTO(
        @JsonProperty("s3_url")
        String s3Url,
        @JsonProperty("detected_objects")
        List<String> detectedObjects,
        @JsonProperty("description")
        String description
) {}
