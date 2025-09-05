package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
// import com.codingrecipe.board.exception.NotFoundException; // [삭제] NotFoundException 임포트 제거
import com.codingrecipe.board.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SurvivalServiceImpl implements SurvivalService {

    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final SurvivalCategoryRepository survivalCategoryRepository;
    private final SeriesRepository seriesRepository;
    private final EpisodesRepository episodesRepository;
    private final ChoicesRepository choicesRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserSeriesCompletionRepository userSeriesCompletionRepository;
    private final UserLevelRepository userLevelRepository;

    @Override
    @Transactional(readOnly = true)
    public SeriesListResponseDTO getSeriesList(Long categoryId) {
        SurvivalCategoryEntity category = survivalCategoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        List<SeriesItemDTO> seriesList = category.getSeriesList().stream()
                .map(seriesEntity -> new SeriesItemDTO(seriesEntity.getSeriesId(), seriesEntity.getTitle()))
                .collect(Collectors.toList());

        return new SeriesListResponseDTO(
                category.getCategoryId(),
                category.getCategoryTitle(),
                category.getCategoryDescription(),
                seriesList
        );
    }

    @Override
    public ChoiceClickResponseDTO choose(String email, Long choiceId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ChoicesEntity choice = choicesRepository.findById(choiceId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        UserProgressEntity userProgress = UserProgressEntity.builder()
                .member(member)
                .series(choice.getEpisode().getSeries())
                .episode(choice.getEpisode())
                .choice(choice)
                .playedAt(LocalDateTime.now())
                .build();
        userProgressRepository.save(userProgress);

        EpisodesEntity nextEpisode = choice.getNextEpisode();
        if (nextEpisode == null) {
            return new ChoiceClickResponseDTO(null, "시리즈의 엔딩에 도달했습니다.");
        }

        return new ChoiceClickResponseDTO(nextEpisode.getEpisodeId(), "다음 에피소드로 이동합니다.");
    }

    @Override
    public Long startSeries(String email, Long seriesId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        SeriesEntity series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        userProgressRepository.deleteByMember_IdAndSeries_SeriesId(member.getId(), seriesId);

        return series.getEpisodes().stream()
                .min(Comparator.comparing(EpisodesEntity::getOrderSeries))
                .map(EpisodesEntity::getEpisodeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeResponseDTO getEpisode(Long episodeId) {
        EpisodesEntity episodeEntity = episodesRepository.findById(episodeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        EpisodeResponseDTO dto = new EpisodeResponseDTO();
        dto.setEpisodeId(episodeEntity.getEpisodeId());
        dto.setTitle(episodeEntity.getEpisodeTitle());

        try {
            List<String> contentList = objectMapper.readValue(episodeEntity.getEpisodeContent(), new TypeReference<>() {});
            dto.setContent(contentList);
        } catch (JsonProcessingException e) {
            dto.setContent(Collections.emptyList());
        }

        List<ChoiceDTO> choiceDTOs = episodeEntity.getChoices().stream().map(choiceEntity -> {
            ChoiceDTO choiceDTO = new ChoiceDTO();
            choiceDTO.setChoiceId(choiceEntity.getChoiceId());

            if (choiceEntity.getNextEpisode() != null) {
                choiceDTO.setNextEpisodeId(choiceEntity.getNextEpisode().getEpisodeId());
            } else {
                choiceDTO.setNextEpisodeId(null);
            }

            try {
                Map<String, String> descriptionMap = objectMapper.readValue(choiceEntity.getChoiceDescription(), new TypeReference<>() {});
                choiceDTO.setDescription(descriptionMap);
            } catch (JsonProcessingException e) {
                choiceDTO.setDescription(Collections.emptyMap());
            }
            return choiceDTO;
        }).collect(Collectors.toList());

        dto.setChoices(choiceDTOs);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeHistoryDTO> getHistory(Long seriesId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<UserProgressEntity> progressList = userProgressRepository.findHistory(member.getId(), seriesId);

        return progressList.stream()
                .map(progress -> new EpisodeHistoryDTO(
                        progress.getEpisode().getEpisodeId(),
                        progress.getChoice().getChoiceDescription(),
                        progress.getPlayedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public boolean completeSeries(String email, Long seriesId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        SeriesEntity series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (userSeriesCompletionRepository.findByMemberAndSeries_SeriesId(member, seriesId).isPresent()) {
            return false;
        }

        UserSeriesCompletionEntity completionEntity = UserSeriesCompletionEntity.builder()
                .member(member)
                .series(series)
                .completedAt(LocalDateTime.now())
                .build();
        userSeriesCompletionRepository.save(completionEntity);

        updateUserLevel(member);
        return true;
    }

    @Override
    public void resetSeries(String email, Long seriesId) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        userProgressRepository.deleteByMember_IdAndSeries_SeriesId(member.getId(), seriesId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserLevelDTO> getUserLevel(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return userLevelRepository.findByMember(member)
                .map(userLevelEntity -> UserLevelDTO.builder()
                        .userId(userLevelEntity.getMember().getId())
                        .completedSeriesCount(userLevelEntity.getCompletedSeriesCount())
                        .levelName(userLevelEntity.getLevelName())
                        .build());
    }

    private void updateUserLevel(Member member) {
        List<UserSeriesCompletionEntity> completedSeriesList = userSeriesCompletionRepository.findByMember(member);
        int completedCount = completedSeriesList.size();
        String newLevelName = getLevelName(completedCount);

        UserLevelEntity userLevel = userLevelRepository.findByMember(member)
                .orElseGet(() -> UserLevelEntity.builder()
                        .member(member)
                        .completedSeriesCount(0)
                        .levelName("NONE")
                        .build());

        userLevel.updateLevel(completedCount, newLevelName);
        userLevelRepository.save(userLevel);
    }

    private String getLevelName(int completedCount) {
        if (completedCount >= 6) {
            return "Lv3.III - 고급 생존자";
        } else if (completedCount >= 3) {
            return "Lv2.II - 중급 생존자";
        } else if (completedCount >= 1) {
            return "Lv1.I - 초급 생존자";
        } else {
            return "NONE";
        }
    }
}
