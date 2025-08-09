// 사용자가 어떤 게시글에 좋아요를 눌렀는지 기록하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "board_like_table") // 'board_like_table' 테이블과 매핑
public class BoardLikeEntity extends BaseEntity { // 생성/수정 시간 필드를 상속받음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 좋아요 기록의 고유 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 'member_id'로 Member와 조인
    private Member member; // 좋아요를 누른 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id") // 'board_id'로 BoardEntity와 조인
    private BoardEntity board; // 좋아요가 눌린 게시글
}
