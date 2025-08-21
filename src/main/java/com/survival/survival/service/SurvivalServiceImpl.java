package com.survival.survival.service;

import com.codingrecipe.board.repository.MemberRepository;
import com.survival.DTO.*;
import com.survival.Entity.*;
import com.survival.exception.*;
import com.survival.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


///SurvivalService 구현

@Service
public class SurvivalServiceImpl implements SurvivalService {

    private final MemberRepository memberRepository;

    private final SurvivalCategoryRepository survivalcategoryRepository;
    private final SeriesRepository seriesRepository;
    private final EpisodesRepository episodesRepository;
    private final ChoicesRepository choicesRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserSeriesCompletionRepository userSeriesCompletionRepository;
    private final UserLevelRepository userLevelRepository;

    public SurvivalServiceImpl(SurvivalCategoryRepository survivalcategoryRepository, SurvivalCategoryRepository survivalcategoryRepository1,
                               SeriesRepository seriesRepository,
                               EpisodesRepository episodesRepository,
                               ChoicesRepository choicesRepository,
                               UserProgressRepository userProgressRepository,
                               UserSeriesCompletionRepository userSeriesCompletionRepository,
                               UserLevelRepository userLevelRepository,
                               MemberRepository memberRepository) {
        this.survivalcategoryRepository = survivalcategoryRepository;
        this.seriesRepository = seriesRepository;
        this.episodesRepository = episodesRepository;
        this.choicesRepository = choicesRepository;
        this.userProgressRepository = userProgressRepository;
        this.userSeriesCompletionRepository = userSeriesCompletionRepository;
        this.userLevelRepository = userLevelRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public SeriesListResponseDTO getSeriesList(Long categoryId) {
        CategoryEntity category = survivalcategoryRepository.findByCategoryId(categoryId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

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
    @Transactional
    public ChoiceClickResponseDTO choose(Long userId, Long choiceId) {
        // [무결성] Member가 community_db에 실제로 존재하는지 확인
        memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다: " + userId));

        ChoicesEntity choice = choicesRepository.findById(choiceId) // findByChoiceId -> findById
                .orElseThrow(() -> new NotFoundException("선택지를 찾을 수 없습니다: " + choiceId));

        // UserProgressEntity 생성 시 => userId는 ID 나머지는 객체로 저장
        UserProgressEntity userProgress = UserProgressEntity.builder()
                .userId(userId)
                .series(choice.getEpisode().getSeries())
                .episode(choice.getEpisode())
                .choice(choice)
                .playedAt(LocalDateTime.now())
                .build();
        userProgressRepository.save(userProgress);

        Long nextEpisodeId = choice.getNextEpisode().getEpisodeId();
        return new ChoiceClickResponseDTO(nextEpisodeId, "다음 에피소드로 이동합니다.");
    }

    @Override
    public Long startSeries(Long userId, Long seriesId) {
        SeriesEntity series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("시리즈를 찾을 수 없습니다: " + seriesId));

        return series.getEpisodes().stream()
                .min(Comparator.comparing(EpisodesEntity::getOrderSeries))
                .map(EpisodesEntity::getEpisodeId)
                .orElseThrow(() -> new NotFoundException("시리즈에 에피소드가 없습니다: " + seriesId));
    }

    @Override
    public EpisodeResponseDTO getEpisode(Long episodeId) {
        EpisodesEntity episode = episodesRepository.findByEpisodeId(episodeId)
                .orElseThrow(() -> new NotFoundException("에피소드를 찾을 수 없습니다: " + episodeId));

        List<ChoiceDTO> choiceDTOs = episode.getChoices().stream()
                .map(choiceEntity -> new ChoiceDTO(
                        choiceEntity.getChoiceId(),
                        choiceEntity.getChoiceDescription(),
                        choiceEntity.getNextEpisode().getEpisodeId()
                ))
                .collect(Collectors.toList());

        return new EpisodeResponseDTO(
                episode.getEpisodeId(),
                episode.getEpisodeTitle(),
                episode.getEpisodeContent(),
                choiceDTOs
        );
    }

    @Override
    public List<EpisodeHistoryDTO> getHistory(Long seriesId, Long userId) {
        List<UserProgressEntity> progressList = userProgressRepository.findHistory(userId, seriesId);

        return progressList.stream()
                .map(progress -> new EpisodeHistoryDTO(
                        progress.getEpisode().getEpisodeId(),
                        progress.getChoice().getChoiceDescription(),
                        progress.getPlayedAt().toString()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean completeSeries(Long userId, Long seriesId) {
        memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다: " + userId));
        SeriesEntity series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("시리즈를 찾을 수 없습니다: " + seriesId));

        // 이미 완료 상태인지 확인
        if (userSeriesCompletionRepository.findByUserIdAndSeriesId(userId, seriesId).isPresent()) {
            return false;
        }

        UserSeriesCompletionEntity completionEntity = UserSeriesCompletionEntity.builder()
                .userId(userId)
                .series(series)
                .completedAt(LocalDateTime.now())
                .build();
        userSeriesCompletionRepository.save(completionEntity);

        updateUserLv(userId);
        return true;
    }

    @Override
    @Transactional
    public boolean resetSeries(Long userId, Long seriesId) {
        userProgressRepository.deleteByUserIdAndSeriesId(userId, seriesId);
        return true;
    }

    @Override
    public Optional<UserLevelDTO> getUserLevel(Long userId){
        return userLevelRepository.findByUserId(userId)
                .map(userLevelEntity -> UserLevelDTO.builder()
                        .userId(userLevelEntity.getUserId())
                        .completedSeriesCount(userLevelEntity.getCompletedSeriesCount())
                        .levelName(userLevelEntity.getLevelName())
                        .build());
    }

    // LevelSystem에 사용될 메서드: 시리즈 개수에 따른 레벨 이름 반환
    private String getLevelName(int completedCount){
        if(completedCount>=6){
            return "Lv3.III - 고급 생존자";
        } else if (completedCount>=3){
            return "Lv2.II - 중급 생존자";
        } else if (completedCount>=1){
            return "Lv1.I - 초급 생존자";
        } else {
            return "NONE";
        }
    }

    // LevelSystem
    @Transactional
    private void updateUserLv(Long userId){
        memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다: " + userId));

        List<UserSeriesCompletionEntity> completedSeriesList = userSeriesCompletionRepository.findByUserId(userId);
        int completedCount = completedSeriesList.size();

        String newLevelName = getLevelName(completedCount);

        Optional<UserLevelEntity> userLevelOptional = userLevelRepository.findByUserId(userId);
        if (userLevelOptional.isPresent()){
            UserLevelEntity userLevel = userLevelOptional.get();
            userLevel.updateLevel(completedCount, newLevelName);
            userLevelRepository.save(userLevel);
        } else {
            UserLevelEntity newUserLevel = UserLevelEntity.builder()
                    .userId(userId)
                    .completedSeriesCount(completedCount)
                    .levelName(newLevelName)
                    .build();
            userLevelRepository.save(newUserLevel);
        }
    }
}
