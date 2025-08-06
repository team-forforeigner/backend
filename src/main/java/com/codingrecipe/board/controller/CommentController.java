// 게시글 댓글 기능 관련 API 컨트롤러
package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment") // 이 컨트롤러의 모든 API는 /api/comment 경로를 가짐
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    public ResponseEntity<List<CommentDTO>> save(@RequestBody CommentDTO commentDTO) {
        // 댓글 저장 처리
        commentService.save(commentDTO);
        // 댓글 저장 후 해당 게시글의 전체 댓글 목록을 다시 조회하여 반환
        List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
        return new ResponseEntity<>(commentDTOList, HttpStatus.CREATED);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<CommentDTO>> findAll(@PathVariable Long boardId) {
        // 특정 게시글 ID에 해당하는 모든 댓글 목록 조회
        List<CommentDTO> commentDTOList = commentService.findAll(boardId);
        return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
    }
}
