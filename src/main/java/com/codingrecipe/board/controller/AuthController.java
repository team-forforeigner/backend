package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.service.LogoutService;
import com.codingrecipe.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final LogoutService logoutService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMyInfo(@AuthenticationPrincipal String email) {
        UserInfoDto userInfo = memberService.getMemberInfoByEmail(email);
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignUpRequestDto dto) {
        memberService.join(dto);
        return ResponseEntity.ok("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        memberService.verifyEmailByToken(token);
        return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        String token = memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return ResponseEntity.ok(new LoginResponseDto(token));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody EmailRequestDto dto) {
        memberService.sendTempPassword(dto);
        return ResponseEntity.ok("가입하신 이메일로 임시 비밀번호를 발송했습니다");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal String email,
            @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(email, request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다");
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token != null) {
            logoutService.logout(token);
        }
        return ResponseEntity.ok("성공적으로 로그아웃되었습니다");
    }
}