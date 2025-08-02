package com.codingrecipe.board.repository;

import com.codingrecipe.board.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithChoices(@Param("quizId") Long quizId);

    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices")
    List<Quiz> findAllWithChoices();

    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.choices WHERE q.id IN :ids")
    List<Quiz> findAllWithChoicesByIdIn(@Param("ids") List<Long> ids);

    @Query(
            value = "WITH ranked_quizzes AS (" +
                    "    SELECT id, category, ROW_NUMBER() OVER(PARTITION BY category ORDER BY RAND()) as rn" +
                    "    FROM quiz WHERE quiz_type IN (:types) AND id NOT IN (:excludedIds)" + // 조건 추가
                    ") " +
                    "SELECT id FROM ranked_quizzes WHERE rn = 1 ORDER BY RAND() LIMIT 10",
            nativeQuery = true
    )
    List<Long> findRandomQuizIdsDiverseByCategoryAndTypes(@Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    @Query(value = "SELECT id FROM quiz WHERE category = :category AND quiz_type IN (:types) AND id NOT IN (:excludedIds) ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Long> findRandomQuizIdsByCategoryAndTypes(@Param("category") String category, @Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    @Query(value = "SELECT id FROM quiz WHERE quiz_type IN (:types) AND id NOT IN (:excludedIds) ORDER BY RAND() LIMIT 10", nativeQuery = true)
    List<Long> findRandomQuizIdsByTypes(@Param("types") List<String> types, @Param("excludedIds") List<Long> excludedIds);

    @Query(value = "SELECT id FROM quiz WHERE id NOT IN (:excludedIds) AND quiz_type IN (:types) ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Long> findRandomQuizIdsExcludingAndByTypes(@Param("excludedIds") List<Long> excludedIds, @Param("types") List<String> types, @Param("limit") int limit);
}