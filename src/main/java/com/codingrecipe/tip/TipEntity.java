package com.codingrecipe.tip;

import jakarta.persistence.*;
import lombok.*;

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

    @ElementCollection(targetClass = TipCategory.class)
    @CollectionTable(name = "tip_categories", joinColumns = @JoinColumn(name = "tip_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TipCategory category;

}
