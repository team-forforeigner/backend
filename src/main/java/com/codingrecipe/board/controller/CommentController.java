package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 변경: HTML 뷰가 아닌 데이터(JSON)를 반환하는 API 컨트롤러임을 명시
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    // 변경: @ModelAttribute -> @RequestBody
    // HTTP Body에 담겨온 JSON 데이터를 DTO에 매핑
    public ResponseEntity<List<CommentDTO>> save(@RequestBody CommentDTO commentDTO) {
        commentService.save(commentDTO);
        List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
        return new ResponseEntity<>(commentDTOList, HttpStatus.CREATED);
    }

    // 추가: 특정 게시글의 모든 댓글을 조회하는 GET 엔드포인트
    @GetMapping("/{boardId}")
    public ResponseEntity<List<CommentDTO>> findAll(@PathVariable Long boardId) {
        List<CommentDTO> commentDTOList = commentService.findAll(boardId);
        return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
    }
}