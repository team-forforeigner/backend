package com.codingrecipe.board.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// AI 서버의 /describe API 응답을 받기 위한 DTO
public record DescriptionResponse(
        @JsonProperty("description")
        String description
) {}