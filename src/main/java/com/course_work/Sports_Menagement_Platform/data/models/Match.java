package com.course_work.Sports_Menagement_Platform.data.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private boolean isResultPublished;

    @ManyToOne
    @JoinColumn(name = "team1", referencedColumnName = "id")
    private Team team1;

    @ManyToOne
    @JoinColumn(name = "team2", referencedColumnName = "id")
    private Team team2;

//    @OneToOne
//    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
//    private Tournament tournament;

    @OneToOne
    @JoinColumn(name = "slot", referencedColumnName = "id")
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage")
    private Stage stage;

}
