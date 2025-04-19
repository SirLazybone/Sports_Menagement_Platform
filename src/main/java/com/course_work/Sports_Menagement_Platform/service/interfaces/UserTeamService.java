package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserTeamService {
    public UserTeam findById(UUID id);

    @Transactional
    void setPlaying(UUID id, boolean isPlaying);

    @Transactional
    void setCapStatus(UUID id, boolean isCap);

    @Transactional
    void setInvitationStatis(UUID id, InvitationStatus invitationStatus);

    List<UserTeam> findByUserId(UUID id);

}
