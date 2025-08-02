package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponseDTO {
    private boolean isLiked;
    private int likeCount;
}