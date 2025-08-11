// 게시판(커뮤니티) 기능 관련 API 컨트롤러
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/boards") // 이 컨트롤러의 모든 API는 /api/boards 경로를 가짐
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<?> save(@ModelAttribute BoardDTO boardDTO,
                                  @AuthenticationPrincipal String email) {
        // 신규 게시글 저장 (파일 포함 가능)
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
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
        // 전체 게시글 목록 페이징 조회
        Page<BoardDTO> boardPage = boardService.paging(pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<BoardDTO>> findByCategory(@PathVariable Long categoryId,
                                                         @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        // 카테고리별 게시글 목록 페이징 조회
        Page<BoardDTO> boardPage = boardService.pagingByCategory(categoryId, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<?> findMyPosts(@AuthenticationPrincipal String email,
                                         @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        // 현재 로그인된 사용자가 작성한 게시글 목록 페이징 조회
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        Page<BoardDTO> boardPage = boardService.pagingByWriter(email, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BoardDTO>> searchPosts(@RequestParam("keyword") String keyword,
                                                      @PageableDefault(page = 0, size = 5, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        // 키워드로 게시글 검색 결과 페이징 조회
        Page<BoardDTO> boardPage = boardService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(boardPage);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<BoardDTO>> findTop3() {
        // 좋아요 수가 가장 많은 상위 3개 게시글 조회
        List<BoardDTO> top3List = boardService.findTop3ByLikes();
        return ResponseEntity.ok(top3List);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDTO> findById(@PathVariable Long id) {
        // ID로 특정 게시글 상세 정보 조회
        try {
            BoardDTO boardDTO = boardService.findById(id);
            return ResponseEntity.ok(boardDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * 게시글 이미지 조회
     * - 이미지가 첨부된 게시글에 대해 S3에서 이미지 파일을 바이트 배열로 반환
     */
    /*@GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getBoardImage(@PathVariable Long id) {
        try {
            byte[] imageBytes = boardService.getBoardImageBytes(id);
            String contentType = boardService.getBoardImageContentType(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                    .body(imageBytes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @RequestBody BoardDTO boardDTO,
                                         @AuthenticationPrincipal String email) {
        // 특정 게시글 정보 수정 (작성자만 가능)
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            boardService.update(id, boardDTO, email);
            return ResponseEntity.ok("게시글이 수정되었습니다");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long boardId,
                                         @AuthenticationPrincipal String email) {
        // 특정 게시글 삭제 (작성자만 가능)
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            boardService.delete(boardId, email);
            return ResponseEntity.ok("게시글이 삭제되었습니다");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeBoard(@PathVariable("id") Long boardId,
                                       @AuthenticationPrincipal String email) {
        // 게시글 좋아요 추가/취소 (토글 방식)
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            LikeResponseDTO response = boardService.toggleLike(boardId, email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my-likes")
    public ResponseEntity<?> getMyLikedPosts(@AuthenticationPrincipal String email) {
        // 현재 로그인된 사용자가 좋아요 한 게시글 목록 조회
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            List<BoardDTO> myLikedPosts = boardService.getMyLikedPosts(email);
            return ResponseEntity.ok(myLikedPosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
