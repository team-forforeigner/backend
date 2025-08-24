package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.EpisodesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EpisodesRepository extends JpaRepository<EpisodesEntity, Long> {
    Optional<EpisodesEntity> findByEpisodeId(Long episodeId);
}
