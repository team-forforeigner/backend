package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.LoginRequestDto;
import com.codingrecipe.board.dto.LoginResponseDto;
import com.codingrecipe.board.dto.SignUpRequestDto;
import com.codingrecipe.board.dto.UserInfoDto;
import com.codingrecipe.board.dto.EmailRequestDto; // ResetPasswordRequestDto 대신 사용할 DTO 예시
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    // 내 정보 조회 API (수정됨)
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // 이제부터 Principal은 email 입니다.

        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("사용자 정보를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(new UserInfoDto(memberOpt.get()));
    }

    // 자체 회원가입 API (수정됨)
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequestDto dto) {
        try {
            memberService.join(dto);
            return ResponseEntity.ok("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 인증 링크 클릭을 처리하는 API (변경 없음)
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            memberService.verifyEmailByToken(token);
            // 인증 완료 후 로그인 페이지나 메인 페이지로 리다이렉트하는 것이 일반적입니다.
            return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰이거나 인증에 실패했습니다.");
        }
    }

    // 아이디 중복 확인 API 삭제
    // @GetMapping("/check-id") ...

    // 자체 로그인 API (수정됨)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            // LoginRequestDto에 getUserId() 대신 getEmail()이 있어야 합니다.
            String token = memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 아이디 찾기 API 삭제
    // @PostMapping("/find-id") ...

    // 비밀번호 재설정 (임시 비밀번호 발송) API (수정됨)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody EmailRequestDto dto) { // email만 받는 DTO 사용
        try {
            memberService.sendTempPassword(dto.getEmail());
            return ResponseEntity.ok("가입하신 이메일로 임시 비밀번호를 발송했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}