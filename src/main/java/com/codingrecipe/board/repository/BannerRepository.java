// 배너 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BannerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<BannerEntity, Long> {

    // 모든 배너를 표시 순서 오름차순으로 정렬하여 조회
    List<BannerEntity> findAllByOrderByDisplayOrderAsc();

}
