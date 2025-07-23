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
    private String fileUrl;

    private Long categoryId;
    private String categoryName;

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
            // 변경: getName() -> getNickname()
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