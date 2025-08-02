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
    private Long id;
    private String commentWriter;
    private String commentContents;
    private Long boardId;
    private LocalDateTime commentCreatedTime;

    // 부모 댓글의 ID를 주고받기 위한 필드
    private Long parentId;

    // 자식 댓글 목록을 화면으로 보내주기 위한 필드
    private List<CommentDTO> children = new ArrayList<>();

    public static CommentDTO toCommentDTO(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());
        commentDTO.setCommentWriter(commentEntity.getCommentWriter());
        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());
        commentDTO.setBoardId(commentEntity.getBoardEntity().getId());

        // 부모 댓글이 있다면, 부모 댓글의 ID를 DTO에 설정
        if (commentEntity.getParent() != null) {
            commentDTO.setParentId(commentEntity.getParent().getId());
        }

        return commentDTO;
    }
}