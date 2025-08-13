package com.survival.Entity;

import lombok.*;
import jakarta.persistence.*;

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
    private Long episodeId;
    private String episodeTitle;

    @Column(columnDefinition = "TEXT")
    private String episodeContent;
    private Integer orderSeries;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seriesId")
    private SeriesEntity series;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChoicesEntity> choices = new ArrayList<>();
}
