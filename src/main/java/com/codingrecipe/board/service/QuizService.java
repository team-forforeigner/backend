package com.codingrecipe.board.service;

import com.codingrecipe.board.domain.*;
import com.codingrecipe.board.dto.*;
import com.codingrecipe.board.repository.MemberRepository;
import com.codingrecipe.board.repository.QuizAttemptRepository;
import com.codingrecipe.board.repository.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final int XP_PER_LEVEL = 100;
    private static final int QUIZ_SET_SIZE = 10;

    @Transactional(readOnly = true)
    public List<QuizDetailResponse> createQuizSet(Category category, QuizMode mode, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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

    public QuizDetailResponse createQuiz(QuizCreateRequest request) {
        Quiz newQuiz = new Quiz();
        updateQuizFromRequest(newQuiz, request);
        Quiz savedQuiz = quizRepository.save(newQuiz);
        return convertToDetailDto(savedQuiz);
    }

    public QuizDetailResponse updateQuiz(Long quizId, QuizCreateRequest request) {
        Quiz existingQuiz = findQuizById(quizId);
        updateQuizFromRequest(existingQuiz, request);
        return convertToDetailDto(existingQuiz);
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
    public QuizDetailResponse findQuizDetailById(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        return convertToDetailDto(quiz);
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
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
            List<QuizChoice> choices = mapChoicesFromDto(request.getChoices(), quiz, request.getCorrectChoiceIndex());
            quiz.getChoices().addAll(choices);
        } else {
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

        if (correctChoiceIndex >= 0 && correctChoiceIndex < choices.size()) {
            choices.get(correctChoiceIndex).setAnswer(true);
        }
        return choices;
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