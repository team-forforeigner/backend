package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.BoardDTO;
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
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<?> save(@ModelAttribute BoardDTO boardDTO,
                                  @AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Long savedId = boardService.save(boardDTO, email);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
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
    public ResponseEntity<?> findMyPosts(@AuthenticationPrincipal String email,
                                         @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
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
        try {
            boardService.updateHits(id);
            BoardDTO boardDTO = boardService.findById(id);
            return ResponseEntity.ok(boardDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody BoardDTO boardDTO,
                                         @AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            boardService.update(id, boardDTO, email);
            return ResponseEntity.ok("게시글이 수정되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long boardId,
                                         @AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            boardService.delete(boardId, email);
            return ResponseEntity.ok("게시글이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeBoard(@PathVariable("id") Long boardId,
                                       @AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            boolean isLiked = boardService.toggleLike(boardId, email);
            int likeCount = boardService.getLikes(boardId);
            return ResponseEntity.ok(Map.of("isLiked", isLiked, "likeCount", likeCount));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my-likes")
    public ResponseEntity<?> getMyLikedPosts(@AuthenticationPrincipal String email) {
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            List<BoardDTO> myLikedPosts = boardService.getMyLikedPosts(email);
            return ResponseEntity.ok(myLikedPosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}