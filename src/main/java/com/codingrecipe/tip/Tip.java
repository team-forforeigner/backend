package com.codingrecipe.tip;

import jakarta.persistence.*;

@Entity
@Table(name = "tip")
public class Tip {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String answer;

    private String source;

    @Enumerated(EnumType.STRING)
    private TipCategory category;

}
