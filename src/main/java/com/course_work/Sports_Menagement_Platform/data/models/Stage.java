package com.course_work.Sports_Menagement_Platform.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

//    private String name;
    private boolean isPublished;
    private int bestPlace; // лучшее возможное место
    private int worstPlace; // количество команд, которые участвуют на этапе

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Match> matches;
}
