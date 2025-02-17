package com.course_work.Sports_Menagement_Platform.data.models;

import com.course_work.Sports_Menagement_Platform.data.enums.AplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "team_tournament")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamTournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private AplicationStatus aplicationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
}
