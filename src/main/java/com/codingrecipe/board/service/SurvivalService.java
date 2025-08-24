package com.codingrecipe.board.service;

import com.codingrecipe.board.dto.ChoiceClickResponseDTO;
import com.codingrecipe.board.dto.EpisodeHistoryDTO;
import com.codingrecipe.board.dto.EpisodeResponseDTO;
import com.codingrecipe.board.dto.SeriesListResponseDTO;
import com.codingrecipe.board.dto.UserLevelDTO;

import java.util.List;
import java.util.Optional;

public interface SurvivalService {
    SeriesListResponseDTO getSeriesList(Long categoryId);
    ChoiceClickResponseDTO choose(String email, Long choiceId);
    Long startSeries(String email, Long seriesId);
    EpisodeResponseDTO getEpisode(Long episodeId);
    List<EpisodeHistoryDTO> getHistory(Long seriesId, String email);
    boolean completeSeries(String email, Long seriesId);
    void resetSeries(String email, Long seriesId);
    Optional<UserLevelDTO> getUserLevel(String email);
}