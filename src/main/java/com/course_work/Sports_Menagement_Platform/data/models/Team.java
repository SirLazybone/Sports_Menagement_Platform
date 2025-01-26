package com.course_work.Sports_Menagement_Platform.data.models;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Sport sport;
    private int countMembers;
    private int wins;
    private int loses;
    private String logo;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTeam> userTeamList = new ArrayList<>();

    @ManyToMany(mappedBy = "teamList")
    private List<Tournament> tournamentList = new ArrayList<>();

    @ManyToMany(mappedBy = "teams")
    private List<Group> groupList = new ArrayList<>();

}
