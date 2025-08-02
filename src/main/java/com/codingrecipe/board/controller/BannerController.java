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
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    // 노출된 배너 목록 조회 API
    @GetMapping
    public ResponseEntity<List<BannerDTO>> getExposedBanners() {
        List<BannerDTO> banners = bannerService.findExposedBanners();
        return ResponseEntity.ok(banners);
    }
}