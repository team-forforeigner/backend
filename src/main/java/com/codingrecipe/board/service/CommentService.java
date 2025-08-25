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
    private final MemberRepository memberRepository; // MemberRepository 주입

    public void save(String email, CommentDTO commentDTO) {
        // email을 이용해 작성자 Member 엔티티를 조회합니다.
        Member writer = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        CommentEntity parentComment = null;
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        }

        // CommentEntity를 생성할 때 Member 엔티티를 넘겨줍니다.
        CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity, writer, parentComment);
        commentRepository.save(commentEntity);
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

    // --- 댓글 수정 서비스 ---
    public CommentDTO update(Long commentId, String email, CommentDTO commentDTO) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자 본인인지 확인합니다.
        if (commentEntity.getWriter() == null || !commentEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 엔티티의 내용을 업데이트합니다.
        commentEntity.update(commentDTO.getCommentContents());

        // 수정된 엔티티를 DTO로 변환하여 반환합니다.
        return CommentDTO.toCommentDTO(commentEntity);
    }

    // --- 댓글 삭제 서비스 ---
    public void delete(Long commentId, String email) {
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 댓글 작성자 본인인지 확인합니다.
        if (commentEntity.getWriter() == null || !commentEntity.getWriter().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        commentRepository.delete(commentEntity);
    }

    // 관리자용 삭제 기능은 그대로 유지됩니다.
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentRepository.deleteById(commentId);
    }
}