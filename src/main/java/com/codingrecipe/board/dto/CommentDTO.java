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

    // 프론트엔드 요청에 따라 게시글 제목과 카테고리명을 추가합니다.
    private String boardTitle;
    private String categoryName;

    private Long parentId;
    private List<CommentDTO> children = new ArrayList<>();

    public static CommentDTO toCommentDTO(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());

        if (commentEntity.getWriter() != null) {
            commentDTO.setCommentWriter(commentEntity.getWriter().getNickname());
        } else {
            commentDTO.setCommentWriter("탈퇴한 사용자");
        }

        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());

        // 게시글 관련 정보도 함께 DTO에 담아줍니다.
        if (commentEntity.getBoardEntity() != null) {
            commentDTO.setBoardId(commentEntity.getBoardEntity().getId());
            commentDTO.setBoardTitle(commentEntity.getBoardEntity().getBoardTitle());
            if (commentEntity.getBoardEntity().getCategory() != null) {
                commentDTO.setCategoryName(commentEntity.getBoardEntity().getCategory().getName());
            }
        }

        if (commentEntity.getParent() != null) {
            commentDTO.setParentId(commentEntity.getParent().getId());
        }

        return commentDTO;
    }
}
