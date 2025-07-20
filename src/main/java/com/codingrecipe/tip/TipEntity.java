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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String answer;

    private String source;

    @Enumerated(EnumType.STRING)
    private TipCategory category;

}
