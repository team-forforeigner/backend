package com.codingrecipe.board.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="episode")
public class EpisodesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="episode_id")
    private Long episodeId;

    @Column(name="episode_title")
    private String episodeTitle;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="episode_content")
    private String episodeContent;

    @Column(name="order_series")
    private Integer orderSeries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    @ToString.Exclude
    private SeriesEntity series;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<ChoicesEntity> choices = new ArrayList<>();
}