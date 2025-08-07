package com.codingrecipe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnalysisResponse (
        @JsonProperty("detected_objects")
        List<String> detectedObjects
){}
