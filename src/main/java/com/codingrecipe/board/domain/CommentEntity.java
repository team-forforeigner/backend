// 게시글 댓글 및 대댓글 정보를 관리
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
@Table(name = "comment_table") // 'comment_table' 테이블과 매핑
public class CommentEntity extends BaseEntity { // 생성/수정 시간 필드를 상속받음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 댓글 고유 식별자

    @Column(length = 20, nullable = false)
    private String commentWriter; // 댓글 작성자

    @Column
    private String commentContents; // 댓글 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id") // 'board_id'로 BoardEntity와 조인
    private BoardEntity boardEntity; // 이 댓글이 속한 게시글

    // --- 대댓글을 위한 셀프 조인 ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 'parent_id'로 부모 댓글과 조인
    private CommentEntity parent; // 부모 댓글 (이 필드가 null이면 일반 댓글, 아니면 대댓글)

    // 부모 댓글이 삭제되면 자식 댓글들도 함께 삭제됨
    @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<CommentEntity> children = new ArrayList<>(); // 자식 댓글 목록 (대댓글들)

    /**
     * CommentDTO와 연관 엔티티들을 받아 CommentEntity 객체를 생성하는 정적 팩토리 메서드
     */
    public static CommentEntity toSaveEntity(CommentDTO commentDTO, BoardEntity boardEntity, CommentEntity parentComment) {
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
        commentEntity.setCommentContents(commentDTO.getCommentContents());
        commentEntity.setBoardEntity(boardEntity);
        commentEntity.setParent(parentComment); // 부모 댓글 설정 (대댓글이 아니면 null)
        return commentEntity;
    }
}