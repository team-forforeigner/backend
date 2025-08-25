package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.CommentEntity;
import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.BoardRepository;
import com.codingrecipe.board.repository.CommentRepository;
import com.codingrecipe.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public void save(String email, CommentDTO commentDTO) {
        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        CommentEntity parentComment = null;
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity, writer, parentComment);
        commentRepository.save(commentEntity);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findAll(Long boardId) {
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardIdWithWriter(boardId);

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

    public CommentDTO update(Long commentId, String email, CommentDTO commentDTO) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (commentEntity.getWriter() == null || !commentEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        commentEntity.update(commentDTO.getCommentContents());
        return CommentDTO.toCommentDTO(commentEntity);
    }

    public void delete(Long commentId, String email) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (commentEntity.getWriter() == null || !commentEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        commentRepository.delete(commentEntity);
    }

    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentRepository.deleteById(commentId);
    }
}
