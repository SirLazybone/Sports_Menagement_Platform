package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.repositories.UserTeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserTeamService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserTeamServiceImpl implements UserTeamService {
    private final UserTeamRepository userTeamRepository;

    public UserTeamServiceImpl(UserTeamRepository userTeamRepository) {
        this.userTeamRepository = userTeamRepository;
    }


    @Override
    public UserTeam findById(UUID id) {
        return userTeamRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    }


    @Override
    @Transactional
    public void setPlaying(UUID id, boolean isPlaying) {
        UserTeam userTeam = findById(id);
        userTeam.setPlaying(isPlaying);
        userTeamRepository.save(userTeam);

    }

    @Override
    @Transactional
    public void setCapStatus(UUID id, boolean isCap) {
        UserTeam userTeam = findById(id);
        userTeam.setCap(isCap);
        userTeamRepository.save(userTeam);
    }

    @Override
    @Transactional
    public void setInvitationStatis(UUID id, InvitationStatus invitationStatus) {
        UserTeam userTeam = findById(id);
        userTeam.setInvitationStatus(invitationStatus);
        userTeamRepository.save(userTeam);
    }
}
