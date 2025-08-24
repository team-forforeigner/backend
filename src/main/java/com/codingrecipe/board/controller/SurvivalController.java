package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ChoiceClickResponseDTO;
import com.codingrecipe.board.dto.EpisodeHistoryDTO;
import com.codingrecipe.board.dto.EpisodeResponseDTO;
import com.codingrecipe.board.dto.SeriesListResponseDTO;
import com.codingrecipe.board.dto.UserLevelDTO;
import com.codingrecipe.board.service.SurvivalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/survival")
public class SurvivalController {

    private final SurvivalService survivalService;

    public SurvivalController(SurvivalService survivalService) {
        this.survivalService = survivalService;
    }

    @PostMapping("/choose")
    public ResponseEntity<ChoiceClickResponseDTO> choose(
            @AuthenticationPrincipal String email,
            @RequestParam Long choiceId
    ) {
        ChoiceClickResponseDTO response = survivalService.choose(email, choiceId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/categories/{categoryId}/series")
    public ResponseEntity<SeriesListResponseDTO> getSeriesList(@PathVariable Long categoryId) {
        SeriesListResponseDTO response = survivalService.getSeriesList(categoryId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/start")
    public ResponseEntity<Long> startSeries(@AuthenticationPrincipal String email, @RequestParam Long seriesId) {
        Long firstEpisodeId = survivalService.startSeries(email, seriesId);
        return new ResponseEntity<>(firstEpisodeId, HttpStatus.OK);
    }

    @GetMapping("/episodes/{episodeId}")
    public ResponseEntity<EpisodeResponseDTO> getEpisode(@PathVariable Long episodeId) {
        EpisodeResponseDTO response = survivalService.getEpisode(episodeId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<List<EpisodeHistoryDTO>> getHistory(@RequestParam Long seriesId, @AuthenticationPrincipal String email) {
        List<EpisodeHistoryDTO> history = survivalService.getHistory(seriesId, email);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeSeries(@RequestParam Long seriesId, @AuthenticationPrincipal String email) {
        boolean success = survivalService.completeSeries(email, seriesId);
        if (success) {
            return new ResponseEntity<>("시리즈가 완료되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("이미 완료한 시리즈입니다.", HttpStatus.OK);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetSeries(@RequestParam Long seriesId, @AuthenticationPrincipal String email) {
        survivalService.resetSeries(email, seriesId);
        return new ResponseEntity<>("시리즈 상태가 초기화되었습니다.", HttpStatus.OK);
    }

    @GetMapping("/level")
    public ResponseEntity<UserLevelDTO> getUserLevel(@AuthenticationPrincipal String email){
        Optional<UserLevelDTO> userLevelOptional = survivalService.getUserLevel(email);
        return userLevelOptional.map(userLevelDTO-> new ResponseEntity<>(userLevelDTO, HttpStatus.OK))
                .orElseGet(()-> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}