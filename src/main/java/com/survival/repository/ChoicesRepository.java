package com.survival.repository;

import com.survival.Entity.ChoicesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//  Choice 엔티티에 대한 데이터 접근을 담당

@Repository
public interface ChoicesRepository extends JpaRepository<ChoicesEntity, Long> {
    // choiceId로 ChoicesEntity를 조회하는 메서드
    Optional<ChoicesEntity> findByChoiceId(Long choiceId);
}
