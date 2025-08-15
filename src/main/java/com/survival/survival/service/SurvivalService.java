package com.survival.survival.service;

import com.survival.DTO.*;

import java.util.List;
import java.util.Optional;

//  서비스 인터페이스

public interface SurvivalService {

    SeriesListResponseDTO getSeriesList(Long categoryId);

    ChoiceClickResponseDTO choose(Long userId, Long choiceId);

    Long startSeries(Long userId, Long seriesId);

    EpisodeResponseDTO getEpisode(Long episodeId);

    List<EpisodeHistoryDTO> getHistory(Long seriesId, Long userId);

    boolean completeSeries(Long userId, Long seriesId);

    boolean resetSeries(Long userId, Long seriesId);

    Optional<UserLevelDTO>getUserLevel(Long userId);
}