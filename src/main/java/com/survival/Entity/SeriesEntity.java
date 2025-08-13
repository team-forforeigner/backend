package com.survival.Entity;

import lombok.*;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="series")
public class SeriesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seriesId;
    private String title;
    private String seriesDescription;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EpisodesEntity> episodes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;


}
