package com.survival.repository;

import com.survival.Entity.EpisodesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 에피소드 엔티티에 대한 접근 담당

@Repository
public interface EpisodesRepository extends JpaRepository<EpisodesEntity, Long> {
    // episodeId로 EpisodesEntity를 조회하는 메서드
    Optional<EpisodesEntity> findByEpisodeId(Long episodeId);
}
