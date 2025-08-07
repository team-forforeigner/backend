// 사용자가 스크랩한 게시글 정보를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "scrap_table") // 'scrap_table' 테이블과 매핑
public class ScrapEntity extends BaseEntity { // 생성/수정 시간 필드를 상속받음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 스크랩 기록 고유 식별자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 'member_id'로 Member와 조인
    private Member member; // 스크랩한 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id") // 'board_id'로 BoardEntity와 조인
    private BoardEntity board; // 스크랩된 게시글
}
