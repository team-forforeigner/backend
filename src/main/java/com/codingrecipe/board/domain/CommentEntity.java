package com.codingrecipe.board.domain;

import com.codingrecipe.board.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comment_table")
public class CommentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member writer;

    @Column
    private String commentContents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity boardEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent;

    @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CommentEntity> children = new ArrayList<>();

    public static CommentEntity toSaveEntity(CommentDTO commentDTO, BoardEntity boardEntity, Member writer, CommentEntity parentComment) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setWriter(writer);
        commentEntity.setCommentContents(commentDTO.getCommentContents());
        commentEntity.setBoardEntity(boardEntity);
        commentEntity.setParent(parentComment);
        return commentEntity;
    }

    public void update(String newContents) {
        this.commentContents = newContents;
    }
}
