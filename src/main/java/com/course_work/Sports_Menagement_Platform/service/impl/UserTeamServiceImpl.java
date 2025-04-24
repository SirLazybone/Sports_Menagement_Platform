package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.TeamTournament;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TournamentRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserTeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserTeamService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserTeamServiceImpl implements UserTeamService {
    private final UserTeamRepository userTeamRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public UserTeamServiceImpl(UserTeamRepository userTeamRepository, UserRepository userRepository, TeamRepository teamRepository, TournamentRepository tournamentRepository) {
        this.userTeamRepository = userTeamRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
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

    @Override
    public List<UserTeam> findByUserId(UUID id) {
        return userTeamRepository.findByUser(userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден")));
    }

    @Override
    public boolean isOnlyCap(UserTeam userTeam) {
        List<UserTeam> caps = userTeam.getTeam().getUserTeamList().stream().filter(i -> i.isCap()).collect(Collectors.toList());
        if (caps.size() == 1 && caps.get(0).getId().equals(userTeam.getId())) {
            return true;
        }
        return false;
    }


}
