package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponseDto<List<CommentDTO>>> save(@RequestBody CommentDTO commentDTO) {
        commentService.save(commentDTO);
        List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(commentDTOList));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponseDto<List<CommentDTO>>> findAll(@PathVariable Long boardId) {
        List<CommentDTO> commentDTOList = commentService.findAll(boardId);
        return ResponseEntity.ok(ApiResponseDto.success(commentDTOList));
    }
}