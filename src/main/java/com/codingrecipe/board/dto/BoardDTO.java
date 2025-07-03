package com.codingrecipe.board.dto;

import com.codingrecipe.board.entity.BoardEntity;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Getter @Setter @ToString
@NoArgsConstructor @AllArgsConstructor
public class BoardDTO {
    private Long id;
    private String boardWriter, boardPass, boardTitle, boardContents;
    private int boardHits, boardLikes, fileAttached;
    private LocalDateTime boardCreatedTime, boardUpdatedTime;
    private MultipartFile boardFile;
    private String originalFileName, storedFileName;

    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, int boardLikes, LocalDateTime boardCreatedTime) {
        this.id = id; this.boardWriter = boardWriter; this.boardTitle = boardTitle;
        this.boardHits = boardHits; this.boardLikes = boardLikes; this.boardCreatedTime = boardCreatedTime;
    }

    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        boardDTO.setBoardLikes(boardEntity.getBoardLikes());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        if (boardEntity.getFileAttached() == 1) {
            boardDTO.setFileAttached(1);
            if (boardEntity.getBoardFileEntityList() != null && !boardEntity.getBoardFileEntityList().isEmpty()) {
                boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
                boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
            }
        } else {
            boardDTO.setFileAttached(0);
        }
        return boardDTO;
    }
}