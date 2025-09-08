package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.QuizMode;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.security.UserPrincipal;
import com.codingrecipe.board.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    /**
     * 사용자를 위한 새로운 퀴즈 묶음을 생성하여 반환합니다.
     */
    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponseDto<Object>> getQuizSet(
            @RequestParam Category category,
            @RequestParam QuizMode mode,
            @AuthenticationPrincipal UserPrincipal user) {
        Object result = quizService.createQuizSet(category, mode, user);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    /**
     * 일반 퀴즈 답안을 제출하고 채점 결과를 받습니다.
     */
    @PostMapping("/quizzes/submit")
    public ResponseEntity<ApiResponseDto<SubmitAnswerResponse>> submitAnswer(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody SubmitAnswerRequest request) {
        // UserPrincipal에서 가져온 ID를 사용하도록 하여 보안을 강화합니다.
        request.setUserId(user.getId());
        SubmitAnswerResponse response = quizService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    /**
     * 보스전 퀴즈 답안을 제출하고 채점 및 보스전 상태 결과를 받습니다.
     */
    @PostMapping("/quizzes/boss/submit")
    public ResponseEntity<ApiResponseDto<SubmitBossAnswerResponseDTO>> submitBossAnswer(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody SubmitAnswerRequest request) {
        SubmitBossAnswerResponseDTO response = quizService.submitBossAnswer(user, request);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    /**
     * 사용자의 퀴즈 힌트 표시 설정을 변경합니다.
     */
    @PatchMapping("/users/me/hint")
    public ResponseEntity<ApiResponseDto<Void>> updateHintSetting(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody HintSettingRequest request) {
        quizService.updateHintSetting(user, request.isEnabled());
        return ResponseEntity.ok(ApiResponseDto.success("힌트 설정이 변경되었습니다."));
    }

    /**
     * 현재 로그인된 사용자가 틀렸던 문제 목록을 조회합니다.
     */
    @GetMapping("/users/me/incorrect-quizzes")
    public ResponseEntity<ApiResponseDto<List<QuizDetailResponse>>> getMyIncorrectQuizzes(@AuthenticationPrincipal UserPrincipal user) {
        List<QuizDetailResponse> incorrectQuizzes = quizService.getIncorrectQuizzes(user);
        return ResponseEntity.ok(ApiResponseDto.success(incorrectQuizzes));
    }

    /**
     * 현재 로그인된 사용자의 전체 퀴즈 풀이 기록을 조회합니다.
     */
    @GetMapping("/users/me/history")
    public ResponseEntity<ApiResponseDto<List<QuizAttemptResponse>>> getMyAttemptHistory(@AuthenticationPrincipal UserPrincipal user) {
        List<QuizAttemptResponse> history = quizService.getAttemptHistory(user);
        return ResponseEntity.ok(ApiResponseDto.success(history));
    }

    /**
     * 전체 사용자 경험치 랭킹을 조회합니다.
     */
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponseDto<List<UserProfileResponse>>> getRanking() {
        List<UserProfileResponse> ranking = quizService.getRanking();
        return ResponseEntity.ok(ApiResponseDto.success(ranking));
    }
}

