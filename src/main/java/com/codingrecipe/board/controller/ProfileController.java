package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.ProfileResponseDto;
import com.codingrecipe.board.dto.ProfileUpdateRequestDto;
import com.codingrecipe.board.security.UserPrincipal;
import com.codingrecipe.board.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final MemberService memberService;

    /**
     * 현재 로그인된 사용자의 프로필 정보를 조회하는 API
     * (선택 가능한 칭호/배지 목록 포함)
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> getMyProfile(@AuthenticationPrincipal UserPrincipal user) {
        ProfileResponseDto userProfile = memberService.getUserProfile(user.getId());
        return ResponseEntity.ok(ApiResponseDto.success(userProfile));
    }

    /**
     * 현재 로그인된 사용자의 프로필 정보를 수정하는 API
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponseDto<Void>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestPart(value = "profileData", required = false) ProfileUpdateRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "backgroundImage", required = false) MultipartFile backgroundImage
    ) throws IOException {
        memberService.updateProfile(user.getId(), requestDto, profileImage, backgroundImage);
        return ResponseEntity.ok(ApiResponseDto.success("프로필이 성공적으로 업데이트되었습니다."));
    }
}

