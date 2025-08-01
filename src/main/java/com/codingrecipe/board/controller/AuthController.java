package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.service.LogoutService;
import com.codingrecipe.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final LogoutService logoutService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        try {
            UserInfoDto userInfo = memberService.getMemberInfoByEmail(email);
            return ResponseEntity.ok(userInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequestDto dto) {
        try {
            memberService.join(dto);
            return ResponseEntity.ok("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        try {
            memberService.verifyEmailByToken(token);
            return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰이거나 인증에 실패했습니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            String token = memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody EmailRequestDto dto) {
        try {
            memberService.sendTempPassword(dto);
            return ResponseEntity.ok("가입하신 이메일로 임시 비밀번호를 발송했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal String email,
            @RequestBody PasswordChangeRequest request) {
        try {
            memberService.changePassword(email, request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token != null) {
            long expiration = jwtUtil.getExpiration(token);
            logoutService.logout(token, expiration);
        }
        return ResponseEntity.ok("성공적으로 로그아웃되었습니다.");
    }
}