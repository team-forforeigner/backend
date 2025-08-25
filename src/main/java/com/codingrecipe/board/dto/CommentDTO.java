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
    private String commentWriter; // 작성자 닉네임
    private String commentContents;
    private Long boardId;
    private LocalDateTime commentCreatedTime;

    private Long parentId;
    private List<CommentDTO> children = new ArrayList<>();

    public static CommentDTO toCommentDTO(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());

        // --- Member 엔티티에서 닉네임을 가져오도록 변경 ---
        if (commentEntity.getWriter() != null) {
            commentDTO.setCommentWriter(commentEntity.getWriter().getNickname());
        } else {
            commentDTO.setCommentWriter("탈퇴한 사용자");
        }

        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());
        commentDTO.setBoardId(commentEntity.getBoardEntity().getId());

        if (commentEntity.getParent() != null) {
            commentDTO.setParentId(commentEntity.getParent().getId());
        }

        return commentDTO;
    }
}
