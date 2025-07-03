package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.entity.BoardEntity;
import com.codingrecipe.board.entity.CommentEntity;
import com.codingrecipe.board.repository.BoardRepository;
import com.codingrecipe.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    public Long save(CommentDTO commentDTO) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(commentDTO.getBoardId());
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity);
            return commentRepository.save(commentEntity).getId();
        } else {
            return null;
        }
    }

    public List<CommentDTO> findAll(Long boardId) {
        // boardId로 부모 엔티티를 조회합니다. Optional로 감싸서 안전하게 처리합니다.
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(boardId);
        if (optionalBoardEntity.isPresent()) {
            BoardEntity boardEntity = optionalBoardEntity.get();
            List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntityOrderByIdDesc(boardEntity);

            List<CommentDTO> commentDTOList = new ArrayList<>();
            for (CommentEntity commentEntity: commentEntityList) {
                // [수정] toCommentDTO 메소드의 파라미터를 하나만 넘기도록 수정
                CommentDTO commentDTO = CommentDTO.toCommentDTO(commentEntity);
                commentDTOList.add(commentDTO);
            }
            return commentDTOList;
        } else {
            // 게시글이 없는 경우 빈 리스트 반환
            return new ArrayList<>();
        }
    }
}