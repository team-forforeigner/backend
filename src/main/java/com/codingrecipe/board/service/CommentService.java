package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.CommentEntity;
import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.BoardRepository;
import com.codingrecipe.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public Long save(CommentDTO commentDTO) {
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        CommentEntity parentComment = null;
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity, parentComment);
        return commentRepository.save(commentEntity).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findAll(Long boardId) {
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntity_IdOrderByCreatedTimeAsc(boardId);

        Map<Long, CommentDTO> commentDTOMap = new HashMap<>();
        commentEntityList.forEach(entity -> {
            CommentDTO dto = CommentDTO.toCommentDTO(entity);
            commentDTOMap.put(dto.getId(), dto);
        });

        List<CommentDTO> rootComments = new ArrayList<>();
        commentEntityList.forEach(entity -> {
            CommentDTO dto = commentDTOMap.get(entity.getId());
            if (entity.getParent() != null) {
                CommentDTO parentDto = commentDTOMap.get(entity.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            } else {
                rootComments.add(dto);
            }
        });

        return rootComments;
    }

    /**
     * 관리자가 ID로 특정 댓글을 삭제합니다.
     * @param commentId 삭제할 댓글의 ID
     */
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentRepository.deleteById(commentId);
    }
}

