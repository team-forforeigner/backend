package com.survival.survival.service;

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
                               UserLevelRepository userLevelRepository) {
        this.survivalcategoryRepository = survivalcategoryRepository;
        this.seriesRepository = seriesRepository;
        this.episodesRepository = episodesRepository;
        this.choicesRepository = choicesRepository;
        this.userProgressRepository = userProgressRepository;
        this.userSeriesCompletionRepository = userSeriesCompletionRepository;
        this.userLevelRepository = userLevelRepository;
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
        ChoicesEntity choice = choicesRepository.findByChoiceId(choiceId)
                .orElseThrow(() -> new NotFoundException("선택지를 찾을 수 없습니다: " + choiceId));

        Long nextEpisodeId = choice.getNextEpisode().getEpisodeId();

        UserProgressEntity userProgress = UserProgressEntity.builder()
                .userId(userId)
                .seriesId(choice.getEpisode().getSeries().getSeriesId())
                .episodeId(choice.getEpisode().getEpisodeId())
                .choiceId(choice.getChoiceId())
                .playedAt(LocalDateTime.now())
                .build();
        userProgressRepository.save(userProgress);

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
        List<UserProgressEntity> progressList = userProgressRepository.findByUserIdAndSeriesId(userId, seriesId);

        return progressList.stream()
                .map(progress -> {
                    ChoicesEntity choice =choicesRepository.findByChoiceId(progress.getChoiceId())
                            .orElseThrow(() -> new NotFoundException("선택지 내용 부재: " + progress.getChoiceId()));
                    return new EpisodeHistoryDTO(
                            progress.getEpisodeId(),
                            choice.getChoiceDescription(),
                            progress.getPlayedAt().toString()
                    );
                })
                .collect(Collectors.toList());
    }

     @Override
     @Transactional
     public boolean completeSeries(Long userId, Long seriesId) {
         // 이미 완료 상태인지 확인
         Optional<UserSeriesCompletionEntity> completion = userSeriesCompletionRepository.findByUserIdAndSeriesId(userId, seriesId);
         if (completion.isPresent()) {
             return false; // 이미 완료된 시리즈는 다시 완료 처리하지 않도록 함
         }

         // 새로운 완료 엔티티 생성 및 저장
         UserSeriesCompletionEntity completionEntity = UserSeriesCompletionEntity.builder()
                 .userId(userId)
                 .seriesId(seriesId)
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
     private void updateUserLv(Long userId){
        //완료한 시리즈 개수 조회(확인)
        List<UserSeriesCompletionEntity> completedSeriesList = userSeriesCompletionRepository.findByUserId(userId);
        int completedCount = completedSeriesList.size();

        //Level finding
        String newLevelName = getLevelName(completedCount);

         // Level info update
         Optional<UserLevelEntity> userLevelOptional = userLevelRepository.findByUserId(userId);
         if (userLevelOptional.isPresent()){
             UserLevelEntity userLevel = userLevelOptional.get();
             userLevel.updateLevel(completedCount, newLevelName);
             userLevelRepository.save(userLevel);
         } else {
             // Level info generate
             UserLevelEntity newUserLevel = UserLevelEntity.builder()
                     .userId(userId)
                     .completedSeriesCount(completedCount)
                     .levelName(newLevelName)
                     .build();
             userLevelRepository.save(newUserLevel);
         }

     }
}
