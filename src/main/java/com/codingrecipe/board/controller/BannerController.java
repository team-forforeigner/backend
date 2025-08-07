// 사용자에게 노출되는 배너 조회 API 컨트롤러
package com.codingrecipe.board.controller;

import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners") // 이 컨트롤러의 모든 API는 /api/banners 경로를 가짐
@RequiredArgsConstructor
public class BannerController {

    // 배너 관련 비즈니스 로직을 처리하는 서비스
    private final BannerService bannerService;

    // 현재 활성화 상태인 배너 목록을 조회
    @GetMapping
    public ResponseEntity<List<BannerDTO>> getExposedBanners() {
        List<BannerDTO> banners = bannerService.findExposedBanners();
        return ResponseEntity.ok(banners);
    }
}
