package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.InvitationTeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.TeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserTeamDTO;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamTournamentRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserTeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    public TeamServiceImpl(TeamRepository teamRepository, UserTeamRepository userTeamRepository) {
        this.teamRepository = teamRepository;
        this.userTeamRepository = userTeamRepository;
    }

    @Override
    public Team getById(UUID id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("No such team"));
    }

    @Override
    public void createTeam(TeamDTO teamDTO, User user) {
        Optional<Team> optionalTeam = teamRepository.findByName(teamDTO.getName());

        if (optionalTeam.isPresent()) {
            throw new RuntimeException("such name is already in use");
        }
        Team team = Team.builder().name(teamDTO.getName()).sport(teamDTO.getSport()).countMembers(1).loses(0).wins(0).build();
        teamRepository.save(team);

        UserTeam userTeam = UserTeam.builder().user(user).team(team).invitationStatus(InvitationStatus.ACCEPTED).isCap(teamDTO.getIsCap()).build();
        userTeamRepository.save(userTeam);
    }

    @Override
    public List<Team> getAllActiveTeamByUser(User user) {
        List<Team> list = userTeamRepository.findAllActiveTeamByUser(user.getId());

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("There are no teams yet");
        }
        return list;
    }

    @Override
    public List<User> getAllUserByTeam(UUID teamId) {
        return userTeamRepository.findAllUserByTeam(teamId);
    }

    @Override
    public List<UserTeamDTO> getAllUserByTeamDTO(UUID teamId) {
        List<UserTeamDTO> userTeamDTOS = userTeamRepository.findAllUserByTeamAndGetDTO(teamId);
        Map<InvitationStatus, Integer> customOrder = Map.of(
                InvitationStatus.ACCEPTED, 1,
                InvitationStatus.PENDING, 2,
                InvitationStatus.DECLINED, 3,
                InvitationStatus.LEFT, 4,
                InvitationStatus.CANCELED, 5,
                InvitationStatus.KICKED, 6
        );
        userTeamDTOS.sort(Comparator.comparing(user -> customOrder.get(user.getInvitationStatus())));
        return userTeamDTOS;
    }

    @Override
    public Boolean isCap(UUID teamId, UUID userId) {
        UserTeam userTeam = userTeamRepository.findByUser_IdAndTeam_Id(userId, teamId).orElseThrow(() -> new RuntimeException("Пользователь не является членом данной команды"));
        return userTeam.isCap();
    }

    @Override
    public List<UserTeam> getActiveInvitations(User user) {
        List<UserTeam> list = userTeamRepository.findByUser(user);
        return list.stream().filter(x -> x.getInvitationStatus() == InvitationStatus.PENDING).toList();
    }

    @Override
    public void createInvitation(UUID teamId, Boolean isCap, User user) {
        Team team = teamRepository.findById(teamId).get();
        Optional<UserTeam> optionalUserTeam = userTeamRepository.findByUser_IdAndTeam_Id(user.getId(), teamId);
        if (optionalUserTeam.isPresent()) {
            UserTeam userTeam = optionalUserTeam.get();
            userTeam.setInvitationStatus(InvitationStatus.PENDING);
            userTeamRepository.save(userTeam);
            return;
        }
        UserTeam userTeam = UserTeam.builder().team(team).user(user).isCap(isCap).invitationStatus(InvitationStatus.PENDING).build();

        team.getUserTeamList().add(userTeam);
        user.getUserTeamList().add(userTeam);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void acceptInvitation(UUID userTeamId) {
        UserTeam userTeam = userTeamRepository.findById(userTeamId).get();
        userTeam.setInvitationStatus(InvitationStatus.ACCEPTED);
        Team team = userTeam.getTeam();
        team.setCountMembers(team.getCountMembers() + 1);

        teamRepository.save(team);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void declineInvitation(UUID userTeamId) {
        UserTeam userTeam = userTeamRepository.findById(userTeamId).get();
        userTeam.setInvitationStatus(InvitationStatus.DECLINED);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void kickUser(UUID teamId, UUID userId) {
        UserTeam userTeam = userTeamRepository.findByUser_IdAndTeam_Id(userId, teamId).get();
        userTeam.setInvitationStatus(InvitationStatus.KICKED);
        Team team = userTeam.getTeam();
        team.setCountMembers(team.getCountMembers() - 1);

        teamRepository.save(team);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void cancelInvitation(UUID teamId, UUID userId) {
        UserTeam userTeam = userTeamRepository.findByUser_IdAndTeam_Id(userId, teamId).get();
        userTeam.setInvitationStatus(InvitationStatus.CANCELED);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void leftTeam(UUID teamId, UUID userId) {
        UserTeam userTeam = userTeamRepository.findByUser_IdAndTeam_Id(userId, teamId).get();
        userTeam.setInvitationStatus(InvitationStatus.LEFT);
        Team team = userTeam.getTeam();
        team.setCountMembers(team.getCountMembers() - 1);

        teamRepository.save(team);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void editTeam(UUID teamId, TeamDTO teamDTO) {
        Optional<Team> optionalTeam = teamRepository.findByName(teamDTO.getName());
        Team team = teamRepository.findById(teamId).get();

        if (optionalTeam.isPresent() && !Objects.equals(optionalTeam.get().getName(), team.getName())) {
            throw new RuntimeException("Данное имя для команды уже занято");
        }
        team.setName(teamDTO.getName());
        team.setSport(teamDTO.getSport());
        teamRepository.save(team);
    }

    @Override
    public Team getByName(String name) {
        return teamRepository.findByName(name).orElseThrow(() -> new RuntimeException("Команды с таким именем не существует"));
    }

}
