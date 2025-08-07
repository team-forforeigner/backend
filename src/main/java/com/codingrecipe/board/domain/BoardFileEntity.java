// 게시글에 첨부된 파일 정보를 관리하는 엔티티
package com.codingrecipe.board.domain;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "board_file_table") // 'board_file_table' 테이블과 매핑
public class BoardFileEntity extends BaseEntity { // 생성/수정 시간 필드를 상속받음
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String originalFileName; // 사용자가 업로드한 원본 파일 이름

    @Column
    private String storedFileName; // 서버에 저장될 때의 파일 이름 (중복 방지를 위함)

    @ManyToOne(fetch = FetchType.LAZY) // 다대일(N:1) 관계 설정
    @JoinColumn(name = "board_id") // 'board_id' 컬럼으로 BoardEntity와 조인
    private BoardEntity boardEntity;

    /**
     * BoardEntity와 파일 이름들을 받아 BoardFileEntity 객체를 생성하는 정적 팩토리 메서드
     */
    public static BoardFileEntity toBoardFileEntity(BoardEntity boardEntity, String originalFileName, String storedFileName) {
        BoardFileEntity boardFileEntity = new BoardFileEntity();
        boardFileEntity.setOriginalFileName(originalFileName);
        boardFileEntity.setStoredFileName(storedFileName);
        boardFileEntity.setBoardEntity(boardEntity); // 연관관계 설정
        return boardFileEntity;
    }
}
