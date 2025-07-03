package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.BoardFileEntity;
import com.codingrecipe.board.repository.BoardFileRepository;
import com.codingrecipe.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;

    @Value("${file-upload-path}")
    private String uploadPath;

    public Page<BoardDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() > 0 ? pageable.getPageNumber() - 1 : 0;
        int pageLimit = 5;
        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));
        return boardEntities.map(board -> new BoardDTO(
                board.getId(), board.getBoardWriter(), board.getBoardTitle(),
                board.getBoardHits(), board.getBoardLikes(), board.getCreatedTime()
        ));
    }

    @Transactional(readOnly = true)
    public List<BoardDTO> findTop3ByLikes() {
        List<BoardEntity> boardEntityList = boardRepository.findTop3ByOrderByBoardLikesDesc();
        return boardEntityList.stream().map(BoardDTO::toBoardDTO).collect(Collectors.toList());
    }

    @Transactional
    public Long save(BoardDTO boardDTO) throws IOException {
        if (boardDTO.getBoardFile() == null || boardDTO.getBoardFile().isEmpty()) {
            return boardRepository.save(BoardEntity.toSaveEntity(boardDTO)).getId();
        } else {
            MultipartFile boardFile = boardDTO.getBoardFile();
            String originalFilename = boardFile.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + originalFilename;
            String savePath = uploadPath + storedFileName;
            File saveFile = new File(savePath);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            boardFile.transferTo(saveFile);
            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
            Long savedId = boardRepository.save(boardEntity).getId();
            BoardEntity savedEntity = boardRepository.findById(savedId).get();
            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(savedEntity, originalFilename, storedFileName);
            boardFileRepository.save(boardFileEntity);
            return savedId;
        }
    }

    @Transactional
    public void updateHits(Long id) { boardRepository.updateHits(id); }

    @Transactional(readOnly = true)
    public BoardDTO findById(Long id) {
        return boardRepository.findById(id).map(BoardDTO::toBoardDTO)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + id));
    }

    @Transactional
    public void update(BoardDTO boardDTO) {
        BoardEntity boardEntity = boardRepository.findById(boardDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id=" + boardDTO.getId()));
        boardEntity.setBoardWriter(boardDTO.getBoardWriter());
        boardEntity.setBoardTitle(boardDTO.getBoardTitle());
        boardEntity.setBoardContents(boardDTO.getBoardContents());
    }

    @Transactional
    public void delete(Long id) { boardRepository.deleteById(id); }

    @Transactional
    public void incrementLikes(Long id) { boardRepository.incrementLikes(id); }
}