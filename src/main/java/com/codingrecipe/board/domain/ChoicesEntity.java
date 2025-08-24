package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="choice")
public class ChoicesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="choice_id")
    private Long choiceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="choice_description")
    private String choiceDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpisodeId")
    @ToString.Exclude
    private EpisodesEntity episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nextEpisodeId")
    @ToString.Exclude
    private EpisodesEntity nextEpisode;
}