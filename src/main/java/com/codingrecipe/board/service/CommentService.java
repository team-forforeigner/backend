// 댓글 및 대댓글 관련 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.CommentEntity;
import com.codingrecipe.board.dto.CommentDTO;
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

    /**
     * 새로운 댓글(또는 대댓글)을 저장
     */
    @Transactional
    public Long save(CommentDTO commentDTO) {
        // 댓글이 달릴 게시글 엔티티 조회
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + commentDTO.getBoardId()));

        // 부모 댓글 엔티티 조회 (대댓글인 경우)
        CommentEntity parentComment = null;
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + commentDTO.getParentId()));
        }

        // DTO를 엔티티로 변환하여 저장
        CommentEntity commentEntity = CommentEntity.toSaveEntity(commentDTO, boardEntity, parentComment);
        return commentRepository.save(commentEntity).getId();
    }

    /**
     * 특정 게시글의 모든 댓글을 계층형 구조(대댓글 포함)로 조회
     */
    @Transactional(readOnly = true)
    public List<CommentDTO> findAll(Long boardId) {
        // 해당 게시글의 모든 댓글을 작성 시간순으로 정렬하여 조회
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntity_IdOrderByCreatedTimeAsc(boardId);

        // 댓글 ID를 키로, DTO를 값으로 갖는 맵을 생성 (빠른 조회를 위함)
        Map<Long, CommentDTO> commentDTOMap = new HashMap<>();
        commentEntityList.forEach(entity -> {
            CommentDTO dto = CommentDTO.toCommentDTO(entity);
            commentDTOMap.put(dto.getId(), dto);
        });

        // 최종적으로 반환할 최상위 댓글(루트 댓글) 목록
        List<CommentDTO> rootComments = new ArrayList<>();
        commentEntityList.forEach(entity -> {
            CommentDTO dto = commentDTOMap.get(entity.getId());
            // 현재 댓글이 대댓글인 경우 (부모가 있는 경우)
            if (entity.getParent() != null) {
                // 맵에서 부모 댓글 DTO를 찾아옴
                CommentDTO parentDto = commentDTOMap.get(entity.getParent().getId());
                if (parentDto != null) {
                    // 부모 댓글의 자식 목록에 현재 댓글 DTO를 추가
                    parentDto.getChildren().add(dto);
                }
            } else {
                // 현재 댓글이 최상위 댓글인 경우 루트 댓글 목록에 추가
                rootComments.add(dto);
            }
        });

        return rootComments;
    }
}
