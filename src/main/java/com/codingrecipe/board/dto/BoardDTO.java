// 게시글 데이터 전송을 위한 DTO 클래스
package com.codingrecipe.board.dto;

import com.codingrecipe.board.domain.BoardEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    // --- 기본 게시글 정보 ---
    private Long id; // 게시글 고유 식별자
    private String boardWriter; // 작성자 닉네임
    private String boardTitle; // 제목
    private String boardContents; // 내용
    private int boardHits; // 조회수
    private int boardLikes; // 좋아요 수
    private LocalDateTime boardCreatedTime; // 작성 시간
    private LocalDateTime boardUpdatedTime; // 수정 시간

    // --- 첨부파일 관련 정보 ---
    private int fileAttached; // 파일 첨부 여부 (1: 첨부, 0: 미첨부)
    private MultipartFile boardFile; // 클라이언트에서 서버로 전달되는 파일 객체
    private String originalFileName; // 원본 파일 이름
    private String storedFileName; // 서버 저장용 파일 이름
    private String fileUrl; // 파일 다운로드/조회 URL

    // --- 카테고리 정보 ---
    private Long categoryId; // 카테고리 ID
    private String categoryName; // 카테고리 이름

    // 목록 조회를 위한 생성자
    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, int boardLikes, LocalDateTime boardCreatedTime, String categoryName) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardLikes = boardLikes;
        this.boardCreatedTime = boardCreatedTime;
        this.categoryName = categoryName;
    }

    // BoardEntity 객체를 BoardDTO 객체로 변환하는 정적 팩토리 메서드
    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(boardEntity.getId());

        // 작성자가 탈퇴한 경우 처리
        if (boardEntity.getWriter() != null) {
            boardDTO.setBoardWriter(boardEntity.getWriter().getNickname());
        } else {
            boardDTO.setBoardWriter("탈퇴한 사용자");
        }

        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        boardDTO.setBoardLikes(boardEntity.getBoardLikes());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        boardDTO.setFileAttached(boardEntity.getFileAttached());

        // 카테고리 정보 매핑
        if (boardEntity.getCategory() != null) {
            boardDTO.setCategoryId(boardEntity.getCategory().getId());
            boardDTO.setCategoryName(boardEntity.getCategory().getName());
        }

        // 첨부 파일이 있는 경우, 첫 번째 파일의 정보를 DTO에 매핑
        if (boardEntity.getFileAttached() == 1 && boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
            boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
            boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
        }

        return boardDTO;
    }
}
