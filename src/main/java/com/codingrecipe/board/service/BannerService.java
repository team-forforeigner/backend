// 배너 생성, 조회, 수정, 삭제 등 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.BannerEntity;
import com.codingrecipe.board.dto.BannerDTO;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
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
                .map(this::convertEntityToDto) // URL 변환을 위해 헬퍼 메서드 사용
                .collect(Collectors.toList());
    }

    public void createBanner(BannerDTO bannerDTO, MultipartFile imageFile) {
        BannerEntity bannerEntity = new BannerEntity();

        s3UploaderService.ifPresentOrElse(
                uploader -> {
                    try {
                        String imageUrl = uploader.uploadImage(imageFile, "banner");
                        bannerEntity.setImageUrl(imageUrl);
                    } catch (IOException e) {
                        log.error("S3 파일 업로드 중 오류 발생", e);
                        throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
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
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));

        if (imageFile != null && !imageFile.isEmpty()) {
            s3UploaderService.ifPresent(uploader -> {
                try {
                    String newImageUrl = uploader.updateImage(bannerEntity.getImageUrl(), imageFile, "banners");
                    bannerEntity.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_FILE_UPLOAD_FAILED);
                }
            });
        }

        bannerEntity.setTitle(bannerDTO.getTitle());
        bannerEntity.setDescription(bannerDTO.getDescription());
        bannerEntity.setLinkUrl(bannerDTO.getLinkUrl());
        bannerEntity.setExposed(bannerDTO.isExposed());
    }

    public void deleteBanner(Long id) {
        BannerEntity bannerEntity = bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.BANNER_NOT_FOUND));

        s3UploaderService.ifPresent(uploader -> {
            // 연결된 S3 이미지 파일 삭제 (DB에 저장된 파일 키 사용)
            uploader.deleteImage(bannerEntity.getImageUrl());
        });

        bannerRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<BannerDTO> findExposedBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc().stream()
                .filter(BannerEntity::isExposed)
                .map(this::convertEntityToDto) // URL 변환을 위해 헬퍼 메서드 사용
                .collect(Collectors.toList());
    }

    // 엔티티 -> DTO 변환 시 S3 파일 키를 완전한 URL로 만들어주는 헬퍼 메서드
    // * 수정 : presigned URL을 사용하지 않도록 변경.
    private BannerDTO convertEntityToDto(BannerEntity entity) {
        BannerDTO dto = BannerDTO.fromEntity(entity);
        /*s3UploaderService.ifPresent(uploader -> {
            String fullUrl = uploader.generatePresignedUrl(entity.getImageUrl());
            dto.setImageUrl(fullUrl); // DTO에는 완전한 URL을 담아준다.
        });*/
        return dto;
    }
}