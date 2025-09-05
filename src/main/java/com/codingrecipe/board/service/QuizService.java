package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.exception.CustomException;
import com.codingrecipe.board.exception.ErrorCode;
import com.codingrecipe.board.repository.*;
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


    private static final int XP_PER_LEVEL = 100;
    private static final int QUIZ_SET_SIZE = 10;

    @Transactional
    public Object createQuizSet(Category category, QuizMode mode, String email) {
        Member member = memberRepository.findByEmail(email)
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
            attemptedQuizIds.add(0L);
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

    public SubmitBossAnswerResponseDTO submitBossAnswer(String email, SubmitAnswerRequest request) {
        Member member = memberRepository.findByEmail(email)
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

    public void updateHintSetting(String email, boolean enabled) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        member.setHintEnabled(enabled);
    }

    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        Quiz quiz = findQuizById(request.getQuizId());
        Member member = memberRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.getUserId()));

        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getUserAnswer().trim());

        member.setPlayCount(member.getPlayCount() + 1);
        updateBadgeBasedOnPlayCount(member);

        if (isCorrect) {
            int earnedXp = request.isFromRetryList() ? 1 : 10;
            member.setExperience(member.getExperience() + earnedXp);

            int requiredXpForNextLevel = member.getLevel() * XP_PER_LEVEL;
            if (member.getExperience() >= requiredXpForNextLevel) {
                member.setLevel(member.getLevel() + 1);
                updateTitleBasedOnLevel(member);
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setMember(member);
        attempt.setQuiz(quiz);
        attempt.setCorrect(isCorrect);
        quizAttemptRepository.save(attempt);

        return new SubmitAnswerResponse(isCorrect, quiz.getExplanation());
    }

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

    @Transactional(readOnly = true)
    public List<QuizDetailResponse> getIncorrectQuizzes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        return quizAttemptRepository.findAllByMemberAndIsCorrectFalse(member)
                .stream()
                .map(QuizAttempt::getQuiz)
                .distinct()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getAttemptHistory(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<QuizAttempt> attempts = quizAttemptRepository.findByMemberIdOrderByAttemptedAtDesc(member.getId());

        return attempts.stream()
                .map(QuizAttemptResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        return new UserProfileResponse(member);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getRanking() {
        List<Member> topUsers = memberRepository.findTop100ByOrderByExperienceDesc();
        return topUsers.stream()
                .map(UserProfileResponse::new)
                .toList();
    }

    private QuizDetailResponse convertToDetailDto(Quiz quiz) {
        if (quiz == null) return null;
        return new QuizDetailResponse(quiz, true);
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

        if (request.getQuizType() == QuizType.MULTIPLE_CHOICE || request.getQuizType() == QuizType.OX) {
            if (request.getCorrectChoiceIndex() == null || request.getChoices() == null) {
                throw new CustomException(ErrorCode.INVALID_ARGUMENT, "객관식/OX 퀴즈는 선택지와 정답 인덱스가 필수입니다.");
            }
            List<QuizChoice> newChoices = new ArrayList<>();
            for (int i = 0; i < request.getChoices().size(); i++) {
                QuizCreateRequest.ChoiceRequest choiceDto = request.getChoices().get(i);
                QuizChoice choice = new QuizChoice();
                choice.setContent(choiceDto.getContent());
                choice.setAnswer(i == request.getCorrectChoiceIndex());
                choice.setQuiz(quiz);
                newChoices.add(choice);
            }
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

    private Quiz findQuizById(Long quizId) {
        return quizRepository.findByIdWithChoices(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));
    }

    private void updateTitleBasedOnLevel(Member member) {
        int level = member.getLevel();
        if (level >= 25) {
            member.setTitle("백호");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/white_tiger.png");
        } else if (level >= 20) {
            member.setTitle("한반도 수호자 호랑이");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/guardian_tiger.png");
        } else if (level >= 15) {
            member.setTitle("100일동안 마늘만 먹고 지낸 곰");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/legend_bear.png");
        } else if (level >= 10) {
            member.setTitle("구미호");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/gumiho.png");
        } else if (level >= 5) {
            member.setTitle("한반도 거북이");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/turtle.png");
        } else {
            member.setTitle("아기 까치");
            member.setBackgroundImageUrl("https://your-s3-bucket/backgrounds/magpie.png");
        }
    }

    private void updateBadgeBasedOnPlayCount(Member member) {
        int count = member.getPlayCount();
        if (count >= 10 && count < 100) {
            member.setBadge("한국어 초보");
        } else if (count >= 100) {
            member.setBadge("명예 한국인");
        }
    }
}

