package com.codingrecipe.tip;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String answer;

    private String source;

    // 카테고리는 다대다 관계로 설정 (컬렉션 타입)
    @ElementCollection(targetClass = TipCategory.class)
    @CollectionTable(name = "tip_categories", joinColumns = @JoinColumn(name = "tip_id"))
    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Set<TipCategory> categories = new HashSet<>();


}
