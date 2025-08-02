package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Category;
import com.codingrecipe.board.domain.Member;
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
    public ResponseEntity<List<QuizDetailResponse>> getQuizSet(
            @RequestParam Category category,
            @RequestParam QuizMode mode,
            @AuthenticationPrincipal String email) {
        List<QuizDetailResponse> quizSet = quizService.createQuizSet(category, mode, email);
        return ResponseEntity.ok(quizSet);
    }

    @PostMapping("/quizzes/submit")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @AuthenticationPrincipal String email,
            @RequestBody SubmitAnswerRequest request) {
        Member member = memberService.findMemberByEmail(email);
        request.setUserId(member.getId());
        SubmitAnswerResponse response = quizService.submitAnswer(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/me/hint")
    public ResponseEntity<Void> updateHintSetting(
            @AuthenticationPrincipal String email,
            @RequestBody HintSettingRequest request) {
        quizService.updateHintSetting(email, request.isEnabled());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/me/nickname")
    public ResponseEntity<String> updateNickname(
            @AuthenticationPrincipal String email,
            @RequestBody NicknameUpdateRequest request) {
        try {
            memberService.updateNickname(email, request.getNickname());
            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal String email) {
        Member member = memberService.findMemberByEmail(email);
        UserProfileResponse userProfile = quizService.getUserProfile(member.getId());
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/users/me/incorrect-quizzes")
    public ResponseEntity<List<QuizDetailResponse>> getMyIncorrectQuizzes(@AuthenticationPrincipal String email) {
        Member member = memberService.findMemberByEmail(email);
        List<QuizDetailResponse> incorrectQuizzes = quizService.getIncorrectQuizzes(member.getId());
        return ResponseEntity.ok(incorrectQuizzes);
    }

    @GetMapping("/users/me/history")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttemptHistory(@AuthenticationPrincipal String email) {
        List<QuizAttemptResponse> history = quizService.getAttemptHistory(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserProfileResponse>> getRanking() {
        List<UserProfileResponse> ranking = quizService.getRanking();
        return ResponseEntity.ok(ranking);
    }
}