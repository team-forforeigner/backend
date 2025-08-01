package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, Long> {

    // 모든 배너를 표시 순서(displayOrder) 오름차순으로 정렬하여 조회합니다.
    List<BannerEntity> findAllByOrderByDisplayOrderAsc();

}