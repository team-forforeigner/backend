package com.codingrecipe.tip.repository;

import com.codingrecipe.tip.domain.TipCategory;
import com.codingrecipe.tip.entity.TipEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TipRepository extends JpaRepository<TipEntity, Long> {

    Page<TipEntity> findByCategory(TipCategory category, Pageable pageable);

    Page<TipEntity> findAll(Pageable pageable);

    boolean existsByQuestion(String question);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM TipEntity t " +
            "WHERE REPLACE(t.question, ' ', '') = :normalizedQuestion")
    boolean existsByQuestionIgnoreSpace(@Param("normalizedQuestion") String normalizedQuestion);

}
