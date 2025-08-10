// 사용자 인증(회원가입, 로그인, 로그아웃) 및 회원 정보 관련 API 컨트롤러
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
@RequestMapping("/api/auth") // 이 컨트롤러의 모든 API는 /api/auth 경로를 가짐
@RequiredArgsConstructor
public class AuthController {

    // 의존성 주입
    private final MemberService memberService;
    private final LogoutService logoutService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo() {
        // 현재 로그인된 사용자의 정보 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // SecurityContext에서 사용자 이메일(ID) 추출
        try {
            UserInfoDto userInfo = memberService.getMemberInfoByEmail(email);
            return ResponseEntity.ok(userInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequestDto dto) {
        // 회원가입 처리 후 인증 이메일 발송
        try {
            memberService.join(dto);
            return ResponseEntity.ok("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        // 이메일 인증 토큰을 검증하여 회원 활성화
        try {
            memberService.verifyEmailByToken(token);
            return ResponseEntity.ok("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰이거나 인증에 실패했습니다");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        // 이메일과 비밀번호로 로그인 처리 후 JWT 토큰 발급
        try {
            String token = memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            return ResponseEntity.ok(new LoginResponseDto(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody EmailRequestDto dto) {
        // 임시 비밀번호를 이메일로 발송
        try {
            memberService.sendTempPassword(dto);
            return ResponseEntity.ok("가입하신 이메일로 임시 비밀번호를 발송했습니다");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal String email, // 현재 로그인된 사용자 이메일 자동 주입
            @RequestBody PasswordChangeRequest request) {
        // 현재 로그인된 사용자의 비밀번호 변경
        try {
            memberService.changePassword(email, request);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 로그아웃 처리 (DB 기반 블랙리스트에 토큰 추가)
        String token = jwtUtil.resolveToken(request); // 요청 헤더에서 JWT 토큰 추출
        if (token != null) {

            // 더 이상 만료 시간을 파라미터로 넘기지 않음
            logoutService.logout(token);
        }
        return ResponseEntity.ok("성공적으로 로그아웃되었습니다");
    }
}
