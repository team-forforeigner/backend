package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.CommentDTO;
import com.codingrecipe.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment") // 댓글 관련 요청은 모두 /comment 로 받습니다.
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    public ResponseEntity<List<CommentDTO>> save(@ModelAttribute CommentDTO commentDTO) {
        // 댓글 저장 처리
        commentService.save(commentDTO);
        // 해당 게시글에 작성된 전체 댓글 목록을 다시 조회
        List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
        // 댓글 목록을 JSON 형태로 반환
        return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
    }
}