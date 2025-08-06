// 게시글의 핵심 정보를 담는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "board_table") // 'board_table' 테이블과 매핑
public class BoardEntity extends BaseEntity { // 생성/수정 시간 필드를 상속받음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 고유 식별자

    @Column
    private String boardTitle; // 게시글 제목

    @Column(length = 500)
    private String boardContents; // 게시글 내용

    @Column
    private int boardHits; // 조회수

    @Column
    private int boardLikes; // 좋아요 수

    @Column
    private int fileAttached; // 파일 첨부 여부 (1: 첨부, 0: 미첨부)

    @ManyToOne(fetch = FetchType.LAZY) // 다대일(N:1) 관계
    @JoinColumn(name = "category_id") // 'category_id'로 CategoryEntity와 조인
    private CategoryEntity category; // 게시글이 속한 카테고리

    @ManyToOne(fetch = FetchType.LAZY) // 다대일(N:1) 관계
    @JoinColumn(name = "member_id") // 'member_id'로 Member와 조인
    private Member writer; // 게시글 작성자

    // 게시글이 삭제되면 연관된 파일들도 함께 삭제됨 (파일 목록)
    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BoardFileEntity> boardFileEntityList = new ArrayList<>();

    // 게시글이 삭제되면 연관된 댓글들도 함께 삭제됨 (댓글 목록)
    @OneToMany(mappedBy = "boardEntity", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentEntity> commentEntityList = new ArrayList<>();
}
