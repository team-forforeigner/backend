package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.QuizMode;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.service.MemberService;
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
    private final MemberService memberService;

    @GetMapping("/quizzes")
    public ResponseEntity<ApiResponseDto<Object>> getQuizSet(
            @RequestParam Category category,
            @RequestParam QuizMode mode,
            @AuthenticationPrincipal String email) {
        Object result = quizService.createQuizSet(category, mode, email);
        return ResponseEntity.ok(ApiResponseDto.success(result));
    }

    @PostMapping("/quizzes/submit")
    public ResponseEntity<ApiResponseDto<SubmitAnswerResponse>> submitAnswer(
            @AuthenticationPrincipal String email,
            @RequestBody SubmitAnswerRequest request) {
        request.setUserId(memberService.findMemberByEmail(email).getId());
        SubmitAnswerResponse response = quizService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @PostMapping("/quizzes/boss/submit")
    public ResponseEntity<ApiResponseDto<SubmitBossAnswerResponseDTO>> submitBossAnswer(
            @AuthenticationPrincipal String email,
            @RequestBody SubmitAnswerRequest request) {
        SubmitBossAnswerResponseDTO response = quizService.submitBossAnswer(email, request);
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @PatchMapping("/users/me/hint")
    public ResponseEntity<ApiResponseDto<Void>> updateHintSetting(
            @AuthenticationPrincipal String email,
            @RequestBody HintSettingRequest request) {
        quizService.updateHintSetting(email, request.isEnabled());
        return ResponseEntity.ok(ApiResponseDto.success("힌트 설정이 변경되었습니다."));
    }

    @PatchMapping("/users/me/nickname")
    public ResponseEntity<ApiResponseDto<Void>> updateNickname(
            @AuthenticationPrincipal String email,
            @RequestBody NicknameUpdateRequest request) {
        memberService.updateNickname(email, request.getNickname());
        return ResponseEntity.ok(ApiResponseDto.success("닉네임이 성공적으로 변경되었습니다."));
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponseDto<UserProfileResponse>> getMyProfile(@AuthenticationPrincipal String email) {
        UserProfileResponse userProfile = quizService.getUserProfile(memberService.findMemberByEmail(email).getId());
        return ResponseEntity.ok(ApiResponseDto.success(userProfile));
    }

    @GetMapping("/users/me/incorrect-quizzes")
    public ResponseEntity<ApiResponseDto<List<QuizDetailResponse>>> getMyIncorrectQuizzes(@AuthenticationPrincipal String email) {
        List<QuizDetailResponse> incorrectQuizzes = quizService.getIncorrectQuizzes(memberService.findMemberByEmail(email).getId());
        return ResponseEntity.ok(ApiResponseDto.success(incorrectQuizzes));
    }

    @GetMapping("/users/me/history")
    public ResponseEntity<ApiResponseDto<List<QuizAttemptResponse>>> getMyAttemptHistory(@AuthenticationPrincipal String email) {
        List<QuizAttemptResponse> history = quizService.getAttemptHistory(email);
        return ResponseEntity.ok(ApiResponseDto.success(history));
    }

    @GetMapping("/ranking")
    public ResponseEntity<ApiResponseDto<List<UserProfileResponse>>> getRanking() {
        List<UserProfileResponse> ranking = quizService.getRanking();
        return ResponseEntity.ok(ApiResponseDto.success(ranking));
    }
}