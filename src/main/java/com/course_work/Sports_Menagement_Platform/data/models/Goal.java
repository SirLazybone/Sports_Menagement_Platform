package com.course_work.Sports_Menagement_Platform.data.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "goal")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private boolean isPenalty; // для футбола или хоккея
    private int time;
    private int points; // для баскетбола
    private int set_number; // для воллейбола

    @OneToOne
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    private Team team;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToOne
    @JoinColumn(name = "match_id", referencedColumnName = "id")
    private Match match;
}
