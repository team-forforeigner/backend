package com.survival.Entity;

import lombok.*;
import jakarta.persistence.*;

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
    private Long choiceId;
    private String choiceDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EpisodeId")
    private EpisodesEntity episode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nextEpisodeId")
    private EpisodesEntity nextEpisode;

}
