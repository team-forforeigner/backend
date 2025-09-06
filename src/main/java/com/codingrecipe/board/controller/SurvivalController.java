package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.ChoiceClickResponseDTO;
import com.codingrecipe.board.dto.EpisodeHistoryDTO;
import com.codingrecipe.board.dto.EpisodeResponseDTO;
import com.codingrecipe.board.dto.SeriesListResponseDTO;
import com.codingrecipe.board.dto.UserLevelDTO;
import com.codingrecipe.board.security.UserPrincipal;
import com.codingrecipe.board.service.SurvivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/survival")
@RequiredArgsConstructor // 생성자 주입을 위해 @RequiredArgsConstructor 사용
public class SurvivalController {

    private final SurvivalService survivalService;

    // 모든 응답을 ApiResponseDto로 감싸도록 변경

    @PostMapping("/choose")
    public ResponseEntity<ApiResponseDto<ChoiceClickResponseDTO>> choose(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam Long choiceId
    ) {
        String email = user.getEmail();
        ChoiceClickResponseDTO response = survivalService.choose(email, choiceId);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/categories/{categoryId}/series")
    public ResponseEntity<ApiResponseDto<SeriesListResponseDTO>> getSeriesList(@PathVariable Long categoryId) {
        SeriesListResponseDTO response = survivalService.getSeriesList(categoryId);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @PostMapping("/start")
    public ResponseEntity<ApiResponseDto<Long>> startSeries(@AuthenticationPrincipal UserPrincipal user, @RequestParam Long seriesId) {
        String email = user.getEmail();
        Long firstEpisodeId = survivalService.startSeries(email, seriesId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(firstEpisodeId));
    }

    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<ApiResponseDto<EpisodeResponseDTO>> getEpisode(@PathVariable Long episodeId) {
        EpisodeResponseDTO response = survivalService.getEpisode(episodeId);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponseDto<List<EpisodeHistoryDTO>>> getHistory(@RequestParam Long seriesId, @AuthenticationPrincipal UserPrincipal user) {
        String email = user.getEmail();
        List<EpisodeHistoryDTO> history = survivalService.getHistory(seriesId, email);
        return ResponseEntity.ok(ApiResponseDto.success(history));
    }

    @PostMapping("/complete")
    public ResponseEntity<ApiResponseDto<String>> completeSeries(@RequestParam Long seriesId, @AuthenticationPrincipal UserPrincipal user) {
        String email = user.getEmail();
        boolean success = survivalService.completeSeries(email, seriesId);
        String message = success ? "시리즈가 완료되었습니다." : "이미 완료한 시리즈입니다.";
        return ResponseEntity.ok(ApiResponseDto.success(message));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponseDto<Void>> resetSeries(@RequestParam Long seriesId, @AuthenticationPrincipal UserPrincipal user) {
        String email = user.getEmail();
        survivalService.resetSeries(email, seriesId);
        return ResponseEntity.ok(ApiResponseDto.success("시리즈 상태가 초기화되었습니다."));
    }

    @GetMapping("/level")
    public ResponseEntity<ApiResponseDto<UserLevelDTO>> getUserLevel(@AuthenticationPrincipal UserPrincipal user){
        String email = user.getEmail();
        Optional<UserLevelDTO> userLevelOptional = survivalService.getUserLevel(email);
        return userLevelOptional
                .map(userLevelDTO -> ResponseEntity.ok(ApiResponseDto.success(userLevelDTO)))
                .orElseGet(()-> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.error(com.codingrecipe.board.exception.ErrorCode.NOT_FOUND)));
    }
}
