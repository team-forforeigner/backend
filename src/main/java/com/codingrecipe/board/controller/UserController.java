package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.ApiResponseDto;
import com.codingrecipe.board.dto.PublicProfileResponseDto;
import com.codingrecipe.board.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final MemberService memberService;

    /**
     * 특정 사용자의 공개 프로필 정보를 조회하는 API
     * @param userId 조회할 사용자의 ID
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponseDto<PublicProfileResponseDto>> getUserProfile(@PathVariable Long userId) {
        PublicProfileResponseDto publicProfile = memberService.getPublicUserProfile(userId);
        return ResponseEntity.ok(ApiResponseDto.success(publicProfile));
    }
}
