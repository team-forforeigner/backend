package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.dto.LikeResponseDTO;
import com.codingrecipe.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Long> save(@ModelAttribute BoardDTO boardDTO,
                                     @AuthenticationPrincipal String email) throws IOException {
        Long savedId = boardService.save(boardDTO, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedId);
    }

    @GetMapping
    public ResponseEntity<Page<BoardDTO>> findAll(@PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.paging(pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<BoardDTO>> findByCategory(@PathVariable Long categoryId,
                                                         @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.pagingByCategory(categoryId, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Page<BoardDTO>> findMyPosts(@AuthenticationPrincipal String email,
                                                      @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.pagingByWriter(email, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BoardDTO>> searchPosts(@RequestParam("keyword") String keyword,
                                                      @PageableDefault(page = 0, size = 5, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<BoardDTO>> findTop3() {
        List<BoardDTO> top3List = boardService.findTop3ByLikes();
        return ResponseEntity.ok(top3List);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> findById(@PathVariable Long id) {
        BoardDTO boardDTO = boardService.findById(id);
        return ResponseEntity.ok(boardDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody BoardDTO boardDTO,
                                         @AuthenticationPrincipal String email) {
        boardService.update(id, boardDTO, email);
        return ResponseEntity.ok("게시글이 수정되었습니다");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long boardId,
                                         @AuthenticationPrincipal String email) {
        boardService.delete(boardId, email);
        return ResponseEntity.ok("게시글이 삭제되었습니다");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponseDTO> likeBoard(@PathVariable("id") Long boardId,
                                                     @AuthenticationPrincipal String email) {
        LikeResponseDTO response = boardService.toggleLike(boardId, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-likes")
    public ResponseEntity<List<BoardDTO>> getMyLikedPosts(@AuthenticationPrincipal String email) {
        List<BoardDTO> myLikedPosts = boardService.getMyLikedPosts(email);
        return ResponseEntity.ok(myLikedPosts);
    }
}