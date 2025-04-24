package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTeamDTO {
    UUID userTeamId;
    boolean isCap;
    boolean isPlaying;
    String name;
    String surname;
    String tel;
    InvitationStatus invitationStatus;
    UUID userId;
}
