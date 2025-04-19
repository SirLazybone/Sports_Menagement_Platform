package com.course_work.Sports_Menagement_Platform.data.models;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_team")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private boolean isCap;
    private InvitationStatus invitationStatus;
    private boolean isPlaying;
}
