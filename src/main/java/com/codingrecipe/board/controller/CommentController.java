package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.security.UserPrincipal; // UserPrincipal 임포트
import com.codingrecipe.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<List<CommentDTO>>> save(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CommentDTO commentDTO) {
        commentService.save(user.getEmail(), commentDTO);
        List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(commentDTOList));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<ApiResponseDto<List<CommentDTO>>> findAllByBoardId(@PathVariable Long boardId) {
        List<CommentDTO> commentDTOList = commentService.findAll(boardId);
        return ResponseEntity.ok(ApiResponseDto.success(commentDTOList));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<CommentDTO>> update(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody CommentDTO commentDTO) {
        CommentDTO updatedComment = commentService.update(commentId, user.getEmail(), commentDTO);
        return ResponseEntity.ok(ApiResponseDto.success(updatedComment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal user) {
        commentService.delete(commentId, user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success("댓글이 성공적으로 삭제되었습니다."));
    }
}
