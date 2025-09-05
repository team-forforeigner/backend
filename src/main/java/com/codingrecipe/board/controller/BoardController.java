package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.dto.LikeResponseDTO;
import com.codingrecipe.board.security.UserPrincipal; // UserPrincipal 임포트
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
    public ResponseEntity<ApiResponseDto<Long>> save(@ModelAttribute BoardDTO boardDTO,
                                                     // [수정] String email 대신 UserPrincipal 객체를 받습니다.
                                                     @AuthenticationPrincipal UserPrincipal user) throws IOException {
        Long savedId = boardService.save(boardDTO, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(savedId));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<BoardDTO>>> findAll(@PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.paging(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(boardPage));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponseDto<Page<BoardDTO>>> findByCategory(@PathVariable Long categoryId,
                                                                         @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.pagingByCategory(categoryId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(boardPage));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponseDto<Page<BoardDTO>>> findMyPosts(@AuthenticationPrincipal UserPrincipal user,
                                                                      @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.pagingByWriter(user.getEmail(), pageable);
        return ResponseEntity.ok(ApiResponseDto.success(boardPage));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<BoardDTO>>> searchPosts(@RequestParam("keyword") String keyword,
                                                                      @PageableDefault(page = 0, size = 5, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<BoardDTO> boardPage = boardService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(boardPage));
    }

    @GetMapping("/top3")
    public ResponseEntity<ApiResponseDto<List<BoardDTO>>> findTop3() {
        List<BoardDTO> top3List = boardService.findTop3ByLikes();
        return ResponseEntity.ok(ApiResponseDto.success(top3List));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BoardDTO>> findById(@PathVariable Long id) {
        BoardDTO boardDTO = boardService.findById(id);
        return ResponseEntity.ok(ApiResponseDto.success(boardDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> update(@PathVariable Long id,
                                                       @RequestBody BoardDTO boardDTO,
                                                       @AuthenticationPrincipal UserPrincipal user) {
        boardService.update(id, boardDTO, user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success("게시글이 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable("id") Long boardId,
                                                       @AuthenticationPrincipal UserPrincipal user) {
        boardService.delete(boardId, user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success("게시글이 삭제되었습니다."));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponseDto<LikeResponseDTO>> likeBoard(@PathVariable("id") Long boardId,
                                                                     @AuthenticationPrincipal UserPrincipal user) {
        LikeResponseDTO response = boardService.toggleLike(boardId, user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/my-likes")
    public ResponseEntity<ApiResponseDto<List<BoardDTO>>> getMyLikedPosts(@AuthenticationPrincipal UserPrincipal user) {
        List<BoardDTO> myLikedPosts = boardService.getMyLikedPosts(user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success(myLikedPosts));
    }
}
