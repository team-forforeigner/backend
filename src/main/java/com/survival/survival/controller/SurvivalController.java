package com.survival.survival.controller;

import com.survival.DTO.ChoiceClickResponseDTO;
import com.survival.DTO.EpisodeHistoryDTO;
import com.survival.DTO.EpisodeResponseDTO;
import com.survival.DTO.SeriesListResponseDTO;
import com.survival.survival.service.SurvivalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// api 컨트롤러

@RestController
@RequestMapping("/api/survival")
public class SurvivalController {

    private final SurvivalService survivalService;

    // 생성자 주입을 통해 SurvivalService 의존성을 주입
    public SurvivalController(SurvivalService survivalService) {
        this.survivalService = survivalService;
    }

    // 선택지 클릭 API
    // POST /api/survival/choose
    @PostMapping("/choose")
    public ResponseEntity<ChoiceClickResponseDTO> choose(
            @RequestParam Long userId,
            @RequestParam Long choiceId
    ) {
        ChoiceClickResponseDTO response = survivalService.choose(userId, choiceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 시리즈 목록 조회 API
    // GET /api/survival/categories/{categoryId}/series
    @GetMapping("/categories/{categoryId}/series")
    public ResponseEntity<SeriesListResponseDTO> getSeriesList(@PathVariable Long categoryId) {
        SeriesListResponseDTO response = survivalService.getSeriesList(categoryId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 시리즈 시작하기 API
    // POST /api/survival/start
    @PostMapping("/start")
    public ResponseEntity<Long> startSeries(@RequestParam Long userId, @RequestParam Long seriesId) {
        Long firstEpisodeId = survivalService.startSeries(userId, seriesId);
        return new ResponseEntity<>(firstEpisodeId, HttpStatus.OK);
    }

    // 에피소드 조회하기 API
    // GET /api/survival/episodes/{episodeId}
    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<EpisodeResponseDTO> getEpisode(@PathVariable Long episodeId) {
        EpisodeResponseDTO response = survivalService.getEpisode(episodeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 엔딩 화면 내용 출력 (에피소드 히스토리 조회) API
    // GET /api/survival/history
    @GetMapping("/history")
    public ResponseEntity<List<EpisodeHistoryDTO>> getHistory(@RequestParam Long seriesId, @RequestParam Long userId) {
        List<EpisodeHistoryDTO> history = survivalService.getHistory(seriesId, userId);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    // 시리즈 완료 처리 API
    // POST /api/survival/complete
    @PostMapping("/complete")
    public ResponseEntity<String> completeSeries(@RequestParam Long seriesId, @RequestParam Long userId) {
        boolean success = survivalService.completeSeries(userId, seriesId);
        if (success) {
            return new ResponseEntity<>("시리즈가 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("시리즈 완료 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 다시 시작 (상태 초기화) API
    // POST /api/survival/reset
    @PostMapping("/reset")
    public ResponseEntity<String> resetSeries(@RequestParam Long seriesId, @RequestParam Long userId) {
        boolean success = survivalService.resetSeries(userId, seriesId);
        if (success) {
            return new ResponseEntity<>("시리즈 상태가 초기화되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("시리즈 상태 초기화 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
