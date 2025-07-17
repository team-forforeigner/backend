package com.codingrecipe.board.controller;

import com.codingrecipe.board.domain.Member;
import com.codingrecipe.board.dto.LoginRequestDto;
import com.codingrecipe.board.dto.LoginResponseDto;
import com.codingrecipe.board.dto.SignUpRequestDto;
import com.codingrecipe.board.dto.UserInfoDto;
import com.codingrecipe.board.dto.FindIdRequestDto;
import com.codingrecipe.board.dto.ResetPasswordRequestDto;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    // 내 정보 조회 API
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        Optional<Member> memberOpt = memberRepository.findByUserId(userId);
        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(404).body("사용자 정보를 찾을 수 없습니다.");
        }
        return ResponseEntity.ok(new UserInfoDto(memberOpt.get()));
    }

    // 자체 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequestDto dto) {
        try {
            memberService.join(dto);
            return ResponseEntity.ok("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이메일 인증 링크 클릭을 처리하는 API
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            memberService.verifyEmailByToken(token);
            return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰이거나 인증에 실패했습니다.");
        }
    }

    // 아이디 중복 확인 API
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkUserId(@RequestParam String userId) {
        return ResponseEntity.ok(memberService.isUserIdDuplicated(userId));
    }

    // 자체 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            String token = memberService.login(loginRequestDto.getUserId(), loginRequestDto.getPassword());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 아이디 찾기 API
    @PostMapping("/find-id")
    public ResponseEntity<?> findUserId(@RequestBody FindIdRequestDto dto) {
        Optional<String> userIdOpt = memberService.findUserIdByNameAndEmail(dto.getName(), dto.getEmail());
        if (userIdOpt.isPresent()) {
            return ResponseEntity.ok(Map.of("userId", userIdOpt.get()));
        } else {
            return ResponseEntity.badRequest().body("일치하는 사용자 정보가 없습니다.");
        }
    }

    // 비밀번호 재설정 API
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequestDto dto) {
        try {
            memberService.resetPassword(dto.getUserId(), dto.getName(), dto.getEmail());
            return ResponseEntity.ok("가입하신 이메일로 임시 비밀번호를 발송했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}