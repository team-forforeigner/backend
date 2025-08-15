package com.survival.repository;

import com.survival.Entity.UserLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLevelRepository extends JpaRepository<UserLevelEntity, Long> {
    //특정 유저의 레벨 정보를 찾습니다
    Optional<UserLevelEntity> findByUserId(Long usreId);

    //완료횟수가 높은 순서대로 랭킹을 조회합니다.... 사용자 경쟁을 위해서만 사용되므로
    //...뺄까요? 경쟁까지 들어가게 되면 만들어야 할 페이지가 많아질 것도 같네요...
    //윤정씨한테 물어봐야돼
    List<UserLevelEntity> findAllByOrderByCompletedSeriesCountDesc();
}
