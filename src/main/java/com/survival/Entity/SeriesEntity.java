package com.survival.Entity;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @Column(name="series_id")
    private Long seriesId;

    @Column(name="title")
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="series_description")
    private String seriesDescription;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<EpisodesEntity> episodes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private CategoryEntity category;

}
