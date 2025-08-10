// 퀴즈 생성, 풀이, 채점, 랭킹 등 퀴즈 관련 모든 비즈니스 로직을 처리하는 서비스
package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.*;
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
    // --- [보스전] 의존성 주입 ---
    private final BossStageRepository bossStageRepository;
    private final BossPhaseRepository bossPhaseRepository;
    private final BossBattleStateRepository bossBattleStateRepository;


    private static final int XP_PER_LEVEL = 100; // 레벨업에 필요한 경험치
    private static final int QUIZ_SET_SIZE = 10; // 한 번에 출제할 퀴즈 개수

    /**
     * 사용자의 설정에 맞는 퀴즈 세트를 생성. 10세트마다 보스전으로 분기.
     * @return 일반 퀴즈일 경우 List<QuizDetailResponse>, 보스전일 경우 BossBattleResponseDTO
     */
    @Transactional
    public Object createQuizSet(Category category, QuizMode mode, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 사용자의 퀴즈 세트 카운트 1 증가
        member.setQuizSetCount(member.getQuizSetCount() + 1);

        // --- [보스전] 10번째 세트마다 보스전 생성 로직으로 분기 ---
        if (member.getQuizSetCount() % 10 == 0) {
            return createBossBattle(member);
        }

        // --- 기존 일반 퀴즈 생성 로직 ---
        boolean hintEnabled = member.isHintEnabled();

        List<Long> attemptedQuizIds = quizAttemptRepository.findByMember(member).stream()
                .map(attempt -> attempt.getQuiz().getId())
                .distinct()
                .collect(Collectors.toList());

        if (attemptedQuizIds.isEmpty()) {
            attemptedQuizIds.add(0L); // 쿼리 오류 방지
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

    /**
     * [보스전] 새로운 보스전을 시작하고 상태를 DB에 저장
     */
    public BossBattleResponseDTO createBossBattle(Member member) {
        // 기존에 진행중인 보스전이 있다면 삭제
        bossBattleStateRepository.findByMember(member).ifPresent(bossBattleStateRepository::delete);

        // 랜덤으로 보스 선택 (여기서는 첫 번째 보스를 가져오는 것으로 단순화)
        BossStage bossStage = bossStageRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("도전할 보스가 없습니다"));

        // 보스의 1페이즈 정보 조회
        BossPhase firstPhase = bossPhaseRepository.findByBossStageAndPhaseNumber(bossStage, 1)
                .orElseThrow(() -> new EntityNotFoundException("보스의 1페이즈 정보를 찾을 수 없습니다"));

        // 새로운 보스전 상태 생성 및 DB 저장
        BossBattleState newState = new BossBattleState();
        newState.setMember(member);
        newState.setBossStage(bossStage);
        newState.setCurrentHp(bossStage.getTotalHp());
        newState.setCurrentPhase(1);
        newState.setCorrectCountInPhase(0);
        newState.setPhaseStartTime(LocalDateTime.now());
        bossBattleStateRepository.save(newState);

        // 1페이즈에 해당하는 퀴즈 목록 조회
        List<Quiz> quizzesForPhase = quizRepository.findByBossPhase(firstPhase);
        List<QuizDetailResponse> quizDTOs = quizzesForPhase.stream()
                .map(quiz -> new QuizDetailResponse(quiz, member.isHintEnabled()))
                .collect(Collectors.toList());

        // 클라이언트에 보낼 DTO 생성
        return new BossBattleResponseDTO(bossStage, firstPhase, quizDTOs);
    }

    /**
     * [보스전] 사용자가 제출한 보스전 답안을 처리
     */
    public SubmitBossAnswerResponseDTO submitBossAnswer(String email, SubmitAnswerRequest request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        BossBattleState state = bossBattleStateRepository.findByMember(member)
                .orElseThrow(() -> new IllegalStateException("진행 중인 보스전 정보가 없습니다"));
        BossPhase currentPhase = bossPhaseRepository.findByBossStageAndPhaseNumber(state.getBossStage(), state.getCurrentPhase())
                .orElseThrow(() -> new EntityNotFoundException("현재 페이즈 정보를 찾을 수 없습니다"));

        // 시간 초과 확인
        long secondsPassed = ChronoUnit.SECONDS.between(state.getPhaseStartTime(), LocalDateTime.now());
        if (secondsPassed > currentPhase.getTimeLimitSeconds()) {
            bossBattleStateRepository.delete(state); // 실패 시 상태 삭제
            return new SubmitBossAnswerResponseDTO(false, state.getCurrentHp(), false, false, true, null);
        }

        Quiz quiz = findQuizById(request.getQuizId());
        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getUserAnswer().trim());

        if (isCorrect) {
            state.setCurrentHp(state.getCurrentHp() - currentPhase.getDamagePerQuiz());
            state.setCorrectCountInPhase(state.getCorrectCountInPhase() + 1);

            // 페이즈 클리어 조건 확인
            if (state.getCorrectCountInPhase() >= currentPhase.getRequiredCorrectAnswers()) {
                // 보스 최종 클리어 조건 확인
                if (state.getCurrentHp() <= 0) {
                    bossBattleStateRepository.delete(state); // 성공 시 상태 삭제
                    // TODO: 보상 지급 로직 (예: 칭호, 배경화면)
                    return new SubmitBossAnswerResponseDTO(true, 0, true, true, false, null);
                } else { // 다음 페이즈로 이동
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
                    nextPhaseData.setCurrentHp(state.getCurrentHp()); // 현재 HP 반영

                    return new SubmitBossAnswerResponseDTO(true, state.getCurrentHp(), true, false, false, nextPhaseData);
                }
            }
        }
        // 정답이지만 페이즈 클리어는 아닐 때, 또는 오답일 때
        return new SubmitBossAnswerResponseDTO(isCorrect, state.getCurrentHp(), false, false, false, null);
    }

    /**
     * 퀴즈 모드에 따라 해당하는 퀴즈 유형 목록을 반환
     */
    private List<String> getTypesForMode(QuizMode mode) {
        if (mode == QuizMode.LIGHT) {
            return List.of(QuizType.OX.name(), QuizType.MULTIPLE_CHOICE.name());
        } else if (mode == QuizMode.STUDY) {
            return List.of(QuizType.SHORT_ANSWER.name());
        }
        return Arrays.stream(QuizType.values()).map(Enum::name).collect(Collectors.toList());
    }

    /**
     * 사용자의 힌트 사용 설정을 업데이트
     */
    public void updateHintSetting(String email, boolean enabled) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        member.setHintEnabled(enabled);
    }

    /**
     * 사용자가 제출한 답안을 채점하고 경험치, 레벨 등을 업데이트
     */
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        Quiz quiz = findQuizById(request.getQuizId());
        Member member = memberRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.getUserId()));

        boolean isCorrect = quiz.getAnswer().equalsIgnoreCase(request.getUserAnswer().trim());

        // 퀴즈 참여 횟수 업데이트 및 칭호 부여
        member.setPlayCount(member.getPlayCount() + 1);
        updateBadgeBasedOnPlayCount(member);

        if (isCorrect) {
            // 정답일 경우 경험치 획득
            int earnedXp = request.isFromRetryList() ? 1 : 10; // 오답노트에서 풀면 1점, 아니면 10점
            member.setExperience(member.getExperience() + earnedXp);

            // 레벨업 체크
            int requiredXpForNextLevel = member.getLevel() * XP_PER_LEVEL;
            if (member.getExperience() >= requiredXpForNextLevel) {
                member.setLevel(member.getLevel() + 1);
                updateTitleBasedOnLevel(member); // 레벨에 따른 칭호 업데이트
            }
        }

        // 퀴즈 풀이 기록 저장
        QuizAttempt attempt = new QuizAttempt();
        attempt.setMember(member);
        attempt.setQuiz(quiz);
        attempt.setCorrect(isCorrect);
        quizAttemptRepository.save(attempt);

        return new SubmitAnswerResponse(isCorrect, quiz.getExplanation());
    }

    /**
     * 새로운 퀴즈를 생성 (관리자용)
     */
    public QuizDetailResponse createQuiz(QuizCreateRequest request) {
        Quiz newQuiz = new Quiz();
        updateQuizFromRequest(newQuiz, request);
        Quiz savedQuiz = quizRepository.save(newQuiz);
        return convertToDetailDto(savedQuiz);
    }

    /**
     * 기존 퀴즈를 수정 (관리자용)
     */
    public QuizDetailResponse updateQuiz(Long quizId, QuizCreateRequest request) {
        Quiz existingQuiz = findQuizById(quizId);
        updateQuizFromRequest(existingQuiz, request);
        return convertToDetailDto(existingQuiz);
    }

    /**
     * 퀴즈를 삭제 (관리자용)
     */
    public void deleteQuiz(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new EntityNotFoundException("삭제할 퀴즈를 찾을 수 없습니다. ID: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    /**
     * 모든 퀴즈 목록을 간략한 정보로 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<QuizSimpleResponse> findAllQuizzes() {
        return quizRepository.findAllWithChoices().stream()
                .map(QuizSimpleResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * ID로 특정 퀴즈의 상세 정보를 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public QuizDetailResponse findQuizDetailById(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        return convertToDetailDto(quiz);
    }

    /**
     * 특정 사용자가 틀렸던 퀴즈 목록(오답 노트)을 조회
     */
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

    /**
     * 특정 사용자의 전체 퀴즈 풀이 기록을 조회
     */
    @Transactional(readOnly = true)
    public List<QuizAttemptResponse> getAttemptHistory(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        List<QuizAttempt> attempts = quizAttemptRepository.findByMemberIdOrderByAttemptedAtDesc(member.getId());

        return attempts.stream()
                .map(QuizAttemptResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자의 프로필 정보를 조회
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        return new UserProfileResponse(member);
    }

    /**
     * 전체 사용자 랭킹을 조회
     */
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

    /**
     * 요청 DTO의 정보로 퀴즈 엔티티의 내용을 업데이트 (생성 및 수정 시 공통 사용)
     */
    private void updateQuizFromRequest(Quiz quiz, QuizCreateRequest request) {
        quiz.setTitle(request.getTitle());
        quiz.setImageUrl(request.getImageUrl());
        quiz.setQuestion(request.getQuestion());
        quiz.setHint(request.getHint());
        quiz.setQuizType(request.getQuizType());
        quiz.setCategory(request.getCategory());
        quiz.setExplanation(request.getExplanation());

        quiz.getChoices().clear(); // 기존 선택지 초기화

        if (request.getQuizType() == QuizType.MULTIPLE_CHOICE || request.getQuizType() == QuizType.OX) {
            List<QuizChoice> choices = mapChoicesFromDto(request.getChoices(), quiz, request.getCorrectChoiceIndex());
            quiz.getChoices().addAll(choices);
        } else { // 주관식인 경우
            QuizChoice answerChoice = new QuizChoice();
            answerChoice.setContent(request.getShortAnswer());
            answerChoice.setAnswer(true);
            answerChoice.setQuiz(quiz);
            quiz.getChoices().add(answerChoice);
        }
    }

    private List<QuizChoice> mapChoicesFromDto(List<QuizCreateRequest.ChoiceRequest> choiceDtos, Quiz quiz, int correctChoiceIndex) {
        if (choiceDtos == null) {
            return new ArrayList<>();
        }
        List<QuizChoice> choices = choiceDtos.stream()
                .filter(choiceDto -> choiceDto.getContent() != null && !choiceDto.getContent().isBlank())
                .map(choiceDto -> {
                    QuizChoice choice = new QuizChoice();
                    choice.setContent(choiceDto.getContent());
                    choice.setAnswer(false);
                    choice.setQuiz(quiz);
                    return choice;
                }).collect(Collectors.toList());

        // 정답 선택지 설정
        if (correctChoiceIndex >= 0 && correctChoiceIndex < choices.size()) {
            choices.get(correctChoiceIndex).setAnswer(true);
        }
        return choices;
    }

    private Quiz findQuizById(Long quizId) {
        return quizRepository.findByIdWithChoices(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));
    }

    /**
     * 레벨에 따라 칭호와 배경 이미지를 업데이트
     */
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

    /**
     * 퀴즈 참여 횟수에 따라 배지를 업데이트
     */
    private void updateBadgeBasedOnPlayCount(Member member) {
        int count = member.getPlayCount();
        if (count >= 10 && count < 100) {
            member.setBadge("한국어 초보");
        } else if (count >= 100) {
            member.setBadge("명예 한국인");
        }
    }
}
