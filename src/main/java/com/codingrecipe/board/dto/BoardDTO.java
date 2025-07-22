package com.codingrecipe.board.dto;

import com.codingrecipe.board.entity.BoardEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private Long id;
    private String boardWriter;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private int boardLikes;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

    private int fileAttached;
    private MultipartFile boardFile;
    private String originalFileName;
    private String storedFileName;
    private String fileUrl; // [추가] Pre-signed URL을 담을 필드

    // 카테고리 정보 필드
    private Long categoryId;
    private String categoryName;

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

    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(boardEntity.getId());

        if (boardEntity.getWriter() != null) {
            boardDTO.setBoardWriter(boardEntity.getWriter().getName());
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

        if (boardEntity.getFileAttached() == 1 && boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
            boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
            boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
        }

        return boardDTO;
    }
}
