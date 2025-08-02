package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.domain.BannerEntity;
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
    private final Optional<S3UploaderService> s3UploaderService;

    @Transactional(readOnly = true)
    public List<BannerDTO> findAllBanners() {
        return bannerRepository.findAll().stream()
                .map(BannerDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void createBanner(BannerDTO bannerDTO, MultipartFile imageFile) {
        BannerEntity bannerEntity = new BannerEntity();

        s3UploaderService.ifPresentOrElse(
                uploader -> {
                    try {
                        String imageUrl = uploader.upload(imageFile, "banners");
                        bannerEntity.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        log.error("S3 파일 업로드 중 오류 발생", e);
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    log.warn("S3UploaderService is not available. Skipping file upload.");
                    bannerEntity.setImageUrl("local-image-path/" + imageFile.getOriginalFilename());
                }
        );

        bannerEntity.setTitle(bannerDTO.getTitle());
        bannerEntity.setDescription(bannerDTO.getDescription());
        bannerEntity.setLinkUrl(bannerDTO.getLinkUrl());
        bannerEntity.setExposed(bannerDTO.isExposed());
        bannerRepository.save(bannerEntity);
    }

    public void updateBanner(Long id, BannerDTO bannerDTO, MultipartFile imageFile) {
        BannerEntity bannerEntity = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 배너를 찾을 수 없습니다. id=" + id));

        if (imageFile != null && !imageFile.isEmpty()) {
            s3UploaderService.ifPresent(uploader -> {
                try {
                    // TODO: 기존 S3 이미지 삭제 로직
                    String newImageUrl = uploader.upload(imageFile, "banners");
                    bannerEntity.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    log.error("S3 파일 업로드 중 오류 발생", e);
                    throw new RuntimeException(e);
                }
            });
        }

        bannerEntity.setTitle(bannerDTO.getTitle());
        bannerEntity.setDescription(bannerDTO.getDescription());
        bannerEntity.setLinkUrl(bannerDTO.getLinkUrl());
        bannerEntity.setExposed(bannerDTO.isExposed());
        bannerRepository.save(bannerEntity);
    }

    public void deleteBanner(Long id) {
        s3UploaderService.ifPresent(uploader -> {
            // TODO: S3 이미지 삭제 로직
        });
        bannerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BannerDTO> findExposedBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(BannerEntity::isExposed)
                .map(BannerDTO::fromEntity)
                .collect(Collectors.toList());
    }
}