package com.course_work.Sports_Menagement_Platform.data.models;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tournament")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Sport sport;
    private int minMembers;
    private LocalDate registerDeadline;
    private String description;
    private boolean is_stopped;
    private String logo;

    @ManyToOne
    @JoinColumn(name = "userOrgCom_id", referencedColumnName = "id")
    private UserOrgCom userOrgCom;

    @ManyToOne
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    private City city;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament")
    private List<TeamTournament> teamTournamentList = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Stage> stages = new ArrayList<>();
}
