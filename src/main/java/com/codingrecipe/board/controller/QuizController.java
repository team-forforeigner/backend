// 퀴즈 풀이, 사용자 프로필, 랭킹 관련 API 컨트롤러
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
@RequestMapping("/api") // 이 컨트롤러의 API는 /api 경로 하위에 위치
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final MemberService memberService;

    @GetMapping("/quizzes")
    public ResponseEntity<?> getQuizSet(
            @RequestParam Category category,
            @RequestParam QuizMode mode,
            @AuthenticationPrincipal String email) {
        // 퀴즈 세트 또는 보스전 데이터를 생성
        Object result = quizService.createQuizSet(category, mode, email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quizzes/submit")
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @AuthenticationPrincipal String email,
            @RequestBody SubmitAnswerRequest request) {
        // 사용자가 제출한 일반 퀴즈 답안 처리
        Member member = memberService.findMemberByEmail(email);
        request.setUserId(member.getId()); // 요청 객체에 사용자 ID 설정
        SubmitAnswerResponse response = quizService.submitAnswer(request);
        return ResponseEntity.ok(response);
    }

    // --- [보스전] 신규 API 추가 ---
    @PostMapping("/quizzes/boss/submit")
    public ResponseEntity<SubmitBossAnswerResponseDTO> submitBossAnswer(
            @AuthenticationPrincipal String email,
            @RequestBody SubmitAnswerRequest request) {
        // 사용자가 제출한 보스전 퀴즈 답안 처리
        SubmitBossAnswerResponseDTO response = quizService.submitBossAnswer(email, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/users/me/hint")
    public ResponseEntity<Void> updateHintSetting(
            @AuthenticationPrincipal String email,
            @RequestBody HintSettingRequest request) {
        // 현재 로그인된 사용자의 힌트 설정(on/off) 변경
        quizService.updateHintSetting(email, request.isEnabled());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/me/nickname")
    public ResponseEntity<String> updateNickname(
            @AuthenticationPrincipal String email,
            @RequestBody NicknameUpdateRequest request) {
        // 현재 로그인된 사용자의 닉네임 변경
        try {
            memberService.updateNickname(email, request.getNickname());
            return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal String email) {
        // 현재 로그인된 사용자의 프로필 정보 조회
        Member member = memberService.findMemberByEmail(email);
        UserProfileResponse userProfile = quizService.getUserProfile(member.getId());
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/users/me/incorrect-quizzes")
    public ResponseEntity<List<QuizDetailResponse>> getMyIncorrectQuizzes(@AuthenticationPrincipal String email) {
        // 현재 로그인된 사용자가 틀렸던 퀴즈 목록 조회
        Member member = memberService.findMemberByEmail(email);
        List<QuizDetailResponse> incorrectQuizzes = quizService.getIncorrectQuizzes(member.getId());
        return ResponseEntity.ok(incorrectQuizzes);
    }

    @GetMapping("/users/me/history")
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttemptHistory(@AuthenticationPrincipal String email) {
        // 현재 로그인된 사용자의 퀴즈 시도 기록 조회
        List<QuizAttemptResponse> history = quizService.getAttemptHistory(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UserProfileResponse>> getRanking() {
        // 전체 사용자 랭킹 조회
        List<UserProfileResponse> ranking = quizService.getRanking();
        return ResponseEntity.ok(ranking);
    }
}
