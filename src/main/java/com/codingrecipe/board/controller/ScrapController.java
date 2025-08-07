// 게시글 스크랩 기능 관련 API 컨트롤러
package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScrapController {

    private final ScrapService scrapService;

    // 게시글 스크랩 추가
    @PostMapping("/boards/{boardId}/scrap")
    public ResponseEntity<String> addScrap(@PathVariable Long boardId,
                                           @AuthenticationPrincipal String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            scrapService.addScrap(userId, boardId);
            return ResponseEntity.ok("게시글을 스크랩했습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 게시글 스크랩 취소
    @DeleteMapping("/boards/{boardId}/scrap")
    public ResponseEntity<String> removeScrap(@PathVariable Long boardId,
                                              @AuthenticationPrincipal String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            scrapService.removeScrap(userId, boardId);
            return ResponseEntity.ok("스크랩을 취소했습니다");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 내가 스크랩한 글 목록 조회
    @GetMapping("/scraps/my")
    public ResponseEntity<?> getMyScraps(@AuthenticationPrincipal String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다");
        }
        try {
            List<BoardDTO> myScraps = scrapService.getMyScraps(userId);
            return ResponseEntity.ok(myScraps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
