package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.domain.BoardEntity;
import com.codingrecipe.board.domain.CommentEntity;
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
        // 1. 부모 엔티티(게시글) 조회
        BoardEntity boardEntity = boardRepository.findById(commentDTO.getBoardId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + commentDTO.getBoardId()));

        CommentEntity parentComment = null;
        // 2. 부모 댓글 ID가 있다면 부모 댓글 엔티티 조회
        if (commentDTO.getParentId() != null) {
            parentComment = commentRepository.findById(commentDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다: " + commentDTO.getParentId()));
        }

        // 3. DTO를 Entity로 변환
        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
        commentEntity.setCommentContents(commentDTO.getCommentContents());
        commentEntity.setBoardEntity(boardEntity);
        commentEntity.setParent(parentComment); // 부모 댓글 설정

        // 4. 리포지토리에 저장 후 ID 반환
        return commentRepository.save(commentEntity).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> findAll(Long boardId) {
        // 1. 해당 게시글의 모든 댓글을 생성 시간 순으로 조회
        List<CommentEntity> commentEntityList = commentRepository.findAllByBoardEntity_IdOrderByCreatedTimeAsc(boardId);

        // 2. 모든 댓글 엔티티를 DTO로 변환하고, Map에 ID를 key로 하여 저장
        Map<Long, CommentDTO> commentDTOMap = new HashMap<>();
        commentEntityList.forEach(entity -> {
            CommentDTO dto = CommentDTO.toCommentDTO(entity);
            commentDTOMap.put(dto.getId(), dto);
        });

        // 3. 계층 구조 조립
        List<CommentDTO> rootComments = new ArrayList<>(); // 최상위 댓글 목록
        commentEntityList.forEach(entity -> {
            CommentDTO dto = commentDTOMap.get(entity.getId());
            if (entity.getParent() != null) {
                // 자식 댓글인 경우: 부모의 children 리스트에 추가
                CommentDTO parentDto = commentDTOMap.get(entity.getParent().getId());
                if(parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            } else {
                // 최상위 댓글인 경우: rootComments 리스트에 추가
                rootComments.add(dto);
            }
        });

        return rootComments;
    }
}