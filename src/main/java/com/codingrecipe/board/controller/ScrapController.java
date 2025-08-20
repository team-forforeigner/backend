package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.BoardDTO;
import com.codingrecipe.board.service.ScrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ScrapController {

    private final ScrapService scrapService;

    @PostMapping("/boards/{boardId}/scrap")
    public ResponseEntity<ApiResponseDto<Void>> addScrap(@PathVariable Long boardId,
                                                         @AuthenticationPrincipal String email) {
        scrapService.addScrap(email, boardId);
        return ResponseEntity.ok(ApiResponseDto.success("게시글을 스크랩했습니다."));
    }

    @DeleteMapping("/boards/{boardId}/scrap")
    public ResponseEntity<ApiResponseDto<Void>> removeScrap(@PathVariable Long boardId,
                                                            @AuthenticationPrincipal String email) {
        scrapService.removeScrap(email, boardId);
        return ResponseEntity.ok(ApiResponseDto.success("스크랩을 취소했습니다."));
    }

    @GetMapping("/scraps/my")
    public ResponseEntity<ApiResponseDto<List<BoardDTO>>> getMyScraps(@AuthenticationPrincipal String email) {
        List<BoardDTO> myScraps = scrapService.getMyScraps(email);
        return ResponseEntity.ok(ApiResponseDto.success(myScraps));
    }
}
