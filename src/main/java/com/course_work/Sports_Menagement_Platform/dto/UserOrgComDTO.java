package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOrgComDTO {
    UUID id;
    String name;
    String surname;
    String tel;
    Org orgRole;
    InvitationStatus invitationStatus;
    boolean isRef;
}
