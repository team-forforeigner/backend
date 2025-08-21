package com.survival.Entity;

import com.codingrecipe.board.domain.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="user_level")
public class UserLevelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userlevelid;

    private Long userId; //외래키

    // 순서 무관 완료한 시리즈의 개수를 저장하는 데이터값
    @Column(nullable = false)
    @ColumnDefault("0")
    private int completedSeriesCount;

    private String levelName;

    // 횟수 , 레벨이름 업데이트용 메소드
    public void updateLevel(int newCompletedCount, String newLevelName){
        this.completedSeriesCount = newCompletedCount;
        this.levelName = newLevelName;
    }
}
