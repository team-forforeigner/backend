package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.security.JwtUtil;
import com.codingrecipe.board.security.UserPrincipal; // UserPrincipal 임포트
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
    // [수정] String email 대신 UserPrincipal 객체를 받습니다.
    public ResponseEntity<ApiResponseDto<UserInfoDto>> getMyInfo(@AuthenticationPrincipal UserPrincipal user) {
        // [수정] UserPrincipal에서 직접 정보를 가져옵니다.
        UserInfoDto userInfo = memberService.getMemberInfoByEmail(user.getEmail());
        return ResponseEntity.ok(ApiResponseDto.success(userInfo));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto<Void>> signup(@Valid @RequestBody SignUpRequestDto dto) {
        memberService.join(dto);
        return ResponseEntity.ok(ApiResponseDto.success("회원가입 요청이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요."));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponseDto<Void>> verifyEmail(@RequestParam("token") String token) {
        memberService.verifyEmailByToken(token);
        return ResponseEntity.ok(ApiResponseDto.success("이메일 인증이 성공적으로 완료되었습니다. 이제 로그인할 수 있습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto) {
        String token = memberService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return ResponseEntity.ok(ApiResponseDto.success(new LoginResponseDto(token)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(@RequestBody EmailRequestDto dto) {
        memberService.sendTempPassword(dto);
        return ResponseEntity.ok(ApiResponseDto.success("가입하신 이메일로 임시 비밀번호를 발송했습니다."));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            // [수정] String email 대신 UserPrincipal 객체를 받습니다.
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(user.getEmail(), request);
        return ResponseEntity.ok(ApiResponseDto.success("비밀번호가 성공적으로 변경되었습니다."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpServletRequest request) {
        String token = jwtUtil.resolveToken(request);
        if (token != null) {
            logoutService.logout(token);
        }
        return ResponseEntity.ok(ApiResponseDto.success("성공적으로 로그아웃되었습니다."));
    }
}
