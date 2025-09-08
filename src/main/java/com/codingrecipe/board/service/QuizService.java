package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.*;
import com.codingrecipe.board.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final MemberRepository memberRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final BossStageRepository bossStageRepository;
    private final BossPhaseRepository bossPhaseRepository;
    private final BossBattleStateRepository bossBattleStateRepository;
    private final TitleAndBadgeManager titleAndBadgeManager;

    private static final int XP_PER_LEVEL = 100;
    private static final int QUIZ_SET_SIZE = 10;

    @Transactional
    public Object createQuizSet(Category category, QuizMode mode, UserPrincipal user) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        member.setQuizSetCount(member.getQuizSetCount() + 1);

        if (member.getQuizSetCount() % 10 == 0) {
            return createBossBattle(member);
        }

        boolean hintEnabled = member.isHintEnabled();

        List<Long> attemptedQuizIds = quizAttemptRepository.findByMember(member).stream()
                .map(attempt -> attempt.getQuiz().getId())
                .distinct()
                .collect(Collectors.toList());

        if (attemptedQuizIds.isEmpty()) {
            attemptedQuizIds.add(0L); // ID가 0인 퀴즈가 없다고 가정하여, 비어있는 IN 절 오류 방지
        }

        List<String> typesForMode = getTypesForMode(mode);
        List<Long> quizIds;

        if (category == Category.ALL) {
            quizIds = quizRepository.findRandomQuizIdsDiverseByCategoryAndTypes(typesForMode, attemptedQuizIds);

            if (quizIds.size() < QUIZ_SET_SIZE) {
                int needed = QUIZ_SET_SIZE - quizIds.size();
                List<Long> currentIdsToExclude = new ArrayList<>(quizIds);
                currentIdsToExclude.addAll(attemptedQuizIds);

                List<Long> additionalQuizIds = quizRepository.findRandomQuizIdsExcludingAndByTypes(currentIdsToExclude, typesForMode, needed);
                quizIds.addAll(additionalQuizIds);
            }
        } else {
            quizIds = quizRepository.findRandomQuizIdsByCategoryAndTypes(category.name(), typesForMode, attemptedQuizIds);
        }

        if (quizIds.isEmpty()) {
            return Collections.emptyList();
        }

        return quizRepository.findAllWithChoicesByIdIn(quizIds).stream()
                .map(quiz -> new QuizDetailResponse(quiz, hintEnabled))
                .collect(Collectors.toList());
    }

    public BossBattleResponseDTO createBossBattle(Member member) {
        bossBattleStateRepository.findByMember(member).ifPresent(bossBattleStateRepository::delete);

        BossStage bossStage = bossStageRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("도전할 보스가 없습니다"));

        BossPhase firstPhase = bossPhaseRepository.findByBossStageAndPhaseNumber(bossStage, 1)
                .orElseThrow(() -> new EntityNotFoundException("보스의 1페이즈 정보를 찾을 수 없습니다"));

        BossBattleState newState = new BossBattleState();
        newState.setMember(member);
        newState.setBossStage(bossStage);
        newState.setCurrentHp(bossStage.getTotalHp());
        newState.setCurrentPhase(1);
        newState.setCorrectCountInPhase(0);
        newState.setPhaseStartTime(LocalDateTime.now());
        bossBattleStateRepository.save(newState);

        List<Quiz> quizzesForPhase = quizRepository.findByBossPhase(firstPhase);
        List<QuizDetailResponse> quizDTOs = quizzesForPhase.stream()
                .map(quiz -> new QuizDetailResponse(quiz, member.isHintEnabled()))
                .collect(Collectors.toList());

        return new BossBattleResponseDTO(bossStage, firstPhase, quizDTOs);
    }

    public SubmitBossAnswerResponseDTO submitBossAnswer(UserPrincipal user, SubmitAnswerRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        BossBattleState state = bossBattleStateRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("진행 중인 보스전 정보가 없습니다"));
        BossPhase currentPhase = bossPhaseRepository.findByBossStageAndPhaseNumber(state.getBossStage(), state.getCurrentPhase())
                .orElseThrow(() -> new EntityNotFoundException("현재 페이즈 정보를 찾을 수 없습니다"));

        long secondsPassed = ChronoUnit.SECONDS.between(state.getPhaseStartTime(), LocalDateTime.now());
        if (secondsPassed > currentPhase.getTimeLimitSeconds()) {
            bossBattleStateRepository.delete(state);
            return new SubmitBossAnswerResponseDTO(false, state.getCurrentHp(), false, false, true, null);
        }

        Quiz quiz = findQuizById(request.getQuizId());
        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getUserAnswer().trim());

        if (isCorrect) {
            state.setCurrentHp(state.getCurrentHp() - currentPhase.getDamagePerQuiz());
            state.setCorrectCountInPhase(state.getCorrectCountInPhase() + 1);

            if (state.getCorrectCountInPhase() >= currentPhase.getRequiredCorrectAnswers()) {
                if (state.getCurrentHp() <= 0) {
                    bossBattleStateRepository.delete(state);
                    return new SubmitBossAnswerResponseDTO(true, 0, true, true, false, null);
                } else {
                    state.setCurrentPhase(state.getCurrentPhase() + 1);
                    state.setCorrectCountInPhase(0);
                    state.setPhaseStartTime(LocalDateTime.now());

                    BossPhase nextPhase = bossPhaseRepository.findByBossStageAndPhaseNumber(state.getBossStage(), state.getCurrentPhase())
                            .orElseThrow(() -> new EntityNotFoundException("다음 페이즈 정보를 찾을 수 없습니다"));
                    List<Quiz> quizzesForNextPhase = quizRepository.findByBossPhase(nextPhase);
                    List<QuizDetailResponse> nextQuizDTOs = quizzesForNextPhase.stream()
                            .map(q -> new QuizDetailResponse(q, member.isHintEnabled()))
                            .collect(Collectors.toList());

                    BossBattleResponseDTO nextPhaseData = new BossBattleResponseDTO(state.getBossStage(), nextPhase, nextQuizDTOs);
                    nextPhaseData.setCurrentHp(state.getCurrentHp());

                    return new SubmitBossAnswerResponseDTO(true, state.getCurrentHp(), true, false, false, nextPhaseData);
                }
            }
        }
        return new SubmitBossAnswerResponseDTO(isCorrect, state.getCurrentHp(), false, false, false, null);
    }

    private List<String> getTypesForMode(QuizMode mode) {
        if (mode == QuizMode.LIGHT) {
            return List.of(QuizType.OX.name(), QuizType.MULTIPLE_CHOICE.name());
        } else if (mode == QuizMode.STUDY) {
            return List.of(QuizType.SHORT_ANSWER.name());
        }
        return Arrays.stream(QuizType.values()).map(Enum::name).collect(Collectors.toList());
    }

    public void updateHintSetting(UserPrincipal user, boolean enabled) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setHintEnabled(enabled);
    }

    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        Member member = memberRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.getUserId()));
        Quiz quiz = findQuizById(request.getQuizId());

        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getUserAnswer().trim());

        member.setPlayCount(member.getPlayCount() + 1);

        if (isCorrect) {
            int earnedXp = request.isFromRetryList() ? 1 : 10;
            member.setExperience(member.getExperience() + earnedXp);

            int requiredXpForNextLevel = member.getLevel() * XP_PER_LEVEL;
            if (member.getExperience() >= requiredXpForNextLevel) {
                int newLevel = member.getLevel() + 1;
                member.setLevel(newLevel);

                // 레벨업 시, 가장 높은 등급의 칭호와 캐릭터를 자동으로 설정
                List<String> titles = titleAndBadgeManager.getAvailableTitles(newLevel);
                if (!titles.isEmpty()) {
                    member.setTitle(titles.get(titles.size() - 1));
                }
                List<AvailableBadgeDto> badges = titleAndBadgeManager.getAvailableBadges(newLevel);
                if(!badges.isEmpty()){
                    member.setBadge(badges.get(badges.size()-1).getImageUrl());
                }
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setMember(member);
        attempt.setQuiz(quiz);
        attempt.setCorrect(isCorrect);
        quizAttemptRepository.save(attempt);

        return new SubmitAnswerResponse(isCorrect, quiz.getExplanation());
    }

    @Transactional(readOnly = true)
    public List<QuizDetailResponse> getIncorrectQuizzes(UserPrincipal user) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return quizAttemptRepository.findAllByMemberAndIsCorrectFalse(member)
                .stream()
                .map(QuizAttempt::getQuiz)
                .distinct()
                .map(quiz -> new QuizDetailResponse(quiz))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getAttemptHistory(UserPrincipal user) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByMemberIdOrderByAttemptedAtDesc(user.getId());
        return attempts.stream()
                .map(QuizAttemptResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getRanking() {
        List<Member> topUsers = memberRepository.findTop100ByOrderByExperienceDesc();
        return topUsers.stream()
                .map(UserProfileResponse::new)
                .toList();
    }

    // --- 관리자용 메소드들 ---
    public QuizAdminDetailResponse createQuiz(QuizCreateRequest request) {
        Quiz newQuiz = new Quiz();
        updateQuizFromRequest(newQuiz, request);
        Quiz savedQuiz = quizRepository.save(newQuiz);
        return new QuizAdminDetailResponse(savedQuiz);
    }

    public QuizAdminDetailResponse updateQuiz(Long quizId, QuizCreateRequest request) {
        Quiz existingQuiz = findQuizById(quizId);
        updateQuizFromRequest(existingQuiz, request);
        Quiz savedQuiz = quizRepository.save(existingQuiz);
        return new QuizAdminDetailResponse(savedQuiz);
    }

    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new EntityNotFoundException("삭제할 퀴즈를 찾을 수 없습니다. ID: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    @Transactional(readOnly = true)
    public List<QuizSimpleResponse> findAllQuizzes() {
        return quizRepository.findAllWithChoices().stream()
                .map(QuizSimpleResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizAdminDetailResponse findQuizDetailById(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        return new QuizAdminDetailResponse(quiz);
    }

    private void updateQuizFromRequest(Quiz quiz, QuizCreateRequest request) {
        quiz.setTitle(request.getTitle());
        quiz.setImageUrl(request.getImageUrl());
        quiz.setQuestion(request.getQuestion());
        quiz.setHint(request.getHint());
        quiz.setQuizType(request.getQuizType());
        quiz.setCategory(request.getCategory());
        quiz.setExplanation(request.getExplanation());

        quiz.getChoices().clear();
        quizRepository.flush();

        if (request.getQuizType() == QuizType.MULTIPLE_CHOICE || request.getQuizType() == QuizType.OX) {
            if (request.getChoices() == null || request.getChoices().isEmpty() || request.getCorrectChoiceIndex() == null) {
                throw new CustomException(ErrorCode.INVALID_ARGUMENT, "객관식/OX 퀴즈는 선택지와 정답 인덱스가 필수입니다.");
            }
            List<QuizChoice> newChoices = mapChoicesFromDto(request.getChoices(), quiz, request.getCorrectChoiceIndex());
            quiz.getChoices().addAll(newChoices);
        } else if (request.getQuizType() == QuizType.SHORT_ANSWER) {
            if (request.getShortAnswer() == null || request.getShortAnswer().isBlank()) {
                throw new CustomException(ErrorCode.INVALID_ARGUMENT, "주관식 퀴즈는 정답이 필수입니다.");
            }
            QuizChoice answerChoice = new QuizChoice();
            answerChoice.setContent(request.getShortAnswer());
            answerChoice.setAnswer(true);
            answerChoice.setQuiz(quiz);
            quiz.getChoices().add(answerChoice);
        }
    }

    private List<QuizChoice> mapChoicesFromDto(List<QuizCreateRequest.ChoiceRequest> choiceDtos, Quiz quiz, int correctChoiceIndex) {
        List<QuizChoice> choices = new ArrayList<>();
        for (int i = 0; i < choiceDtos.size(); i++) {
            QuizCreateRequest.ChoiceRequest dto = choiceDtos.get(i);
            if (dto.getContent() != null && !dto.getContent().isBlank()) {
                QuizChoice choice = new QuizChoice();
                choice.setContent(dto.getContent());
                choice.setAnswer(i == correctChoiceIndex);
                choice.setQuiz(quiz);
                choices.add(choice);
            }
        }
        return choices;
    }

    private Quiz findQuizById(Long quizId) {
        return quizRepository.findByIdWithChoices(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));
    }
}

