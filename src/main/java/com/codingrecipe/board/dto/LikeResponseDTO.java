// 게시글 좋아요 처리 후의 응답 데이터를 담는 DTO
package com.codingrecipe.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponseDTO {
    private boolean isLiked; // 현재 사용자의 좋아요 상태 (true: 좋아요, false: 좋아요 취소)
    private int likeCount;   // 해당 게시글의 총 좋아요 수
}
