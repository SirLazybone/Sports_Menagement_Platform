package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitationStatusDTO {
    InvitationStatus pending = InvitationStatus.PENDING;
    InvitationStatus accepted = InvitationStatus.ACCEPTED;
    InvitationStatus declined = InvitationStatus.DECLINED;
    InvitationStatus left = InvitationStatus.LEFT;
    InvitationStatus canceled = InvitationStatus.CANCELED;
    InvitationStatus kicked = InvitationStatus.KICKED;

}
