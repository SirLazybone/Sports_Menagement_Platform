package com.course_work.Sports_Menagement_Platform.data.models;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Sport sport;
    private String city;
    private int minMembers;
    private Date registerDeadline;
    private String description;
    private boolean is_stopped;
    private String logo;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private User org;

    @OneToOne
    @JoinColumn(name = "orgcom_id", referencedColumnName = "id")
    private OrgCom orgCom;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament")
    private List<TeamTournament> teamTournamentList = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Stage> stages = new ArrayList<>();
}
