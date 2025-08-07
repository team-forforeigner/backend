// 퀴즈 데이터베이스 처리를 위한 리포지토리 인터페이스
package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.BossPhase;
import com.codingrecipe.board.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // --- [보스전] 신규 메서드 추가 ---
    // 특정 페이즈에 속한 모든 퀴즈를 조회
    List<Quiz> findByBossPhase(BossPhase bossPhase);
    // --------------------------------

    // N+1 문제 해결을 위해 퀴즈와 선택지를 함께 조회 (fetch join)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithChoices(@Param("quizId") Long quizId);

    // 모든 퀴즈를 선택지와 함께 조회 (fetch join)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices")
    List<Quiz> findAllWithChoices();

    // 주어진 ID 목록에 해당하는 퀴즈들을 선택지와 함께 조회 (fetch join)
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices WHERE q.id IN :ids")
    List<Quiz> findAllWithChoicesByIdIn(@Param("ids") List<Long> ids);

    // 네이티브 쿼리를 사용하여, 각 카테고리별로 지정된 타입의 퀴즈를 랜덤하게 1개씩, 최대 10개까지 조회
    // 이미 푼 문제는 제외
    @Query(
            value = "WITH ranked_quizzes AS (" +
                    "    SELECT id, category, ROW_NUMBER() OVER(PARTITION BY category ORDER BY RAND()) as rn" +
                    "    FROM quiz WHERE quiz_type IN (:types) AND id NOT IN (:excludedIds)" +
                    ") " +
                    "SELECT id FROM ranked_quizzes WHERE rn = 1 ORDER BY RAND() LIMIT 10",
            nativeQuery = true
    )
    List<Long> findRandomQuizIdsDiverseByCategoryAndTypes(@Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    // 네이티브 쿼리를 사용하여, 특정 카테고리 내에서 지정된 타입의 퀴즈를 랜덤하게 10개 조회
    // 이미 푼 문제는 제외
    @Query(value = "SELECT id FROM quiz WHERE category = :category AND quiz_type IN (:types) AND id NOT IN (:excludedIds) ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Long> findRandomQuizIdsByCategoryAndTypes(@Param("category") String category, @Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    // 네이티브 쿼리를 사용하여, 전체 카테고리에서 지정된 타입의 퀴즈를 랜덤하게 10개 조회
    // 이미 푼 문제는 제외
    @Query(value = "SELECT id FROM quiz WHERE quiz_type IN (:types) AND id NOT IN (:excludedIds) ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Long> findRandomQuizIdsByTypes(@Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    // 네이티브 쿼리를 사용하여, 지정된 타입의 퀴즈를 주어진 개수(limit)만큼 랜덤하게 조회
    // 이미 푼 문제는 제외
    @Query(value = "SELECT id FROM quiz WHERE id NOT IN (:excludedIds) AND quiz_type IN (:types) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Long> findRandomQuizIdsExcludingAndByTypes(@Param("excludedIds") List<Long> excludedIds, @Param("types") List<String> types, @Param("limit") int limit);
}
