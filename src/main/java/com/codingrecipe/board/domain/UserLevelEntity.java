package com.codingrecipe.board.domain;

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
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private Member member;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int completedSeriesCount;

    private String levelName;

    public void updateLevel(int newCompletedCount, String newLevelName){
        this.completedSeriesCount = newCompletedCount;
        this.levelName = newLevelName;
    }
}