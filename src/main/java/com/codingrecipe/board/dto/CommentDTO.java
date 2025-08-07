// 댓글 데이터 전송을 위한 DTO 클래스
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.CommentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Long id; // 댓글 고유 식별자
    private String commentWriter; // 댓글 작성자 닉네임
    private String commentContents; // 댓글 내용
    private Long boardId; // 댓글이 속한 게시글 ID
    private LocalDateTime commentCreatedTime; // 댓글 작성 시간

    // --- 대댓글 처리를 위한 필드 ---
    private Long parentId; // 부모 댓글 ID (대댓글의 경우)
    private List<CommentDTO> children = new ArrayList<>(); // 자식 댓글 목록 (대댓글 목록)

    // CommentEntity 객체를 CommentDTO 객체로 변환하는 정적 팩토리 메서드
    public static CommentDTO toCommentDTO(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());
        commentDTO.setCommentWriter(commentEntity.getCommentWriter());
        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());
        commentDTO.setBoardId(commentEntity.getBoardEntity().getId());

        // 부모 댓글이 있는 경우, 그 ID를 DTO에 설정
        if (commentEntity.getParent() != null) {
            commentDTO.setParentId(commentEntity.getParent().getId());
        }

        return commentDTO;
    }
}
