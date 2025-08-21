package com.survival.Entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.ArrayList;

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
    private List<ChoicesEntity> choices = new ArrayList<>();
}
