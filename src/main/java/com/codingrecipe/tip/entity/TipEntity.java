package com.codingrecipe.tip.entity;

import com.codingrecipe.tip.domain.TipCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "tip", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"question"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String question;

    @NotNull
    @Column(nullable = false)
    private String answer;

    private String source;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipCategory category;

}
