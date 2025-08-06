// 보스전의 보스 정보를 관리하는 엔티티
package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "boss_stage") // 'boss_stage' 테이블과 매핑
public class BossStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 보스 고유 식별자

    @Column(nullable = false, unique = true)
    private String bossName; // 보스 이름 (예: "이순신")

    @Column(nullable = false)
    private int totalHp; // 보스의 총 체력 (예: 1000)

    @Column
    private String bossImageUrl; // 보스 이미지 URL

    // 보스가 삭제되면 연관된 페이즈들도 함께 삭제됨
    @OneToMany(mappedBy = "bossStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BossPhase> phases = new ArrayList<>();
}
