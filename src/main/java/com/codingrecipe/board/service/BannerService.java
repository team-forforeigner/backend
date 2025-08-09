// 배너 생성, 조회, 수정, 삭제 등 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.BannerEntity;
import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final Optional<S3UploaderService> s3UploaderService; // S3 서비스가 local 환경 등에서 비활성화될 수 있으므로 Optional로 주입

    /**
     * 모든 배너 목록을 조회
     */
    @Transactional(readOnly = true)
    public List<BannerDTO> findAllBanners() {
        return bannerRepository.findAll().stream()
                .map(BannerDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 배너를 생성 (이미지 파일 포함)
     */
    public void createBanner(BannerDTO bannerDTO, MultipartFile imageFile) {
        BannerEntity bannerEntity = new BannerEntity();

        // S3 서비스가 활성화되어 있을 때와 아닐 때를 구분하여 처리
        s3UploaderService.ifPresentOrElse(
                // S3 서비스가 있을 경우: 파일을 S3에 업로드하고 URL을 가져옴
                uploader -> {
                    try {
                        String imageUrl = uploader.upload(imageFile, "banners");
                        bannerEntity.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        log.error("S3 파일 업로드 중 오류 발생", e);
                        throw new RuntimeException(e);
                    }
                },
                // S3 서비스가 없을 경우: 로컬 경로를 임시로 설정하고 경고 로그를 남김
                () -> {
                    log.warn("S3UploaderService is not available. Skipping file upload.");
                    bannerEntity.setImageUrl("local-image-path/" + imageFile.getOriginalFilename());
                }
        );

        // DTO의 나머지 정보를 엔티티에 설정
        bannerEntity.setTitle(bannerDTO.getTitle());
        bannerEntity.setDescription(bannerDTO.getDescription());
        bannerEntity.setLinkUrl(bannerDTO.getLinkUrl());
        bannerEntity.setExposed(bannerDTO.isExposed());
        bannerRepository.save(bannerEntity);
    }

    /**
     * 기존 배너 정보를 수정 (이미지 파일은 선택적으로 업데이트)
     */
    public void updateBanner(Long id, BannerDTO bannerDTO, MultipartFile imageFile) {
        BannerEntity bannerEntity = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 배너를 찾을 수 없습니다. id=" + id));

        // 새로운 이미지 파일이 제공된 경우에만 S3 업로드 및 URL 업데이트 수행
        if (imageFile != null && !imageFile.isEmpty()) {
            s3UploaderService.ifPresent(uploader -> {
                try {
                    // TODO: 기존 S3 이미지 삭제 로직 필요
                    String newImageUrl = uploader.upload(imageFile, "banners");
                    bannerEntity.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    log.error("S3 파일 업로드 중 오류 발생", e);
                    throw new RuntimeException(e);
                }
            });
        }

        // 배너의 텍스트 정보 등 업데이트
        bannerEntity.setTitle(bannerDTO.getTitle());
        bannerEntity.setDescription(bannerDTO.getDescription());
        bannerEntity.setLinkUrl(bannerDTO.getLinkUrl());
        bannerEntity.setExposed(bannerDTO.isExposed());
        bannerRepository.save(bannerEntity);
    }

    /**
     * 특정 배너를 삭제
     */
    public void deleteBanner(Long id) {
        // S3 서비스가 활성화된 경우, 연결된 S3 이미지 파일 삭제 로직 수행
        s3UploaderService.ifPresent(uploader -> {
            // TODO: S3 이미지 삭제 로직 필요
        });
        // 데이터베이스에서 배너 정보 삭제
        bannerRepository.deleteById(id);
    }

    /**
     * 사용자에게 노출된 배너 목록만 조회
     */
    @Transactional(readOnly = true)
    public List<BannerDTO> findExposedBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(BannerEntity::isExposed) // 노출된 배너만 필터링
                .map(BannerDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
