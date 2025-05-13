package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.TeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserTeamDTO;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserTeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final UserTeamService userTeamService;



    public TeamServiceImpl(TeamRepository teamRepository, UserTeamRepository userTeamRepository, UserTeamService userTeamService) {
        this.teamRepository = teamRepository;
        this.userTeamRepository = userTeamRepository;
        this.userTeamService = userTeamService;

    }

    @Override
    public Team getById(UUID id) {
        return teamRepository.findById(id).orElseThrow(() -> new RuntimeException("No such team"));
    }

    @Override
    public Team createTeam(TeamDTO teamDTO, User user) {
        Optional<Team> optionalTeam = teamRepository.findByName(teamDTO.getName());
        if (optionalTeam.isPresent()) {
            throw new RuntimeException("Команда с таким именем уже существует, выберете другое");
        }
        Team team = Team.builder().name(teamDTO.getName()).sport(teamDTO.getSport()).logo(teamDTO.getLogo()).build();
        teamRepository.save(team);

        UserTeam userTeam = UserTeam.builder().user(user).team(team).invitationStatus(InvitationStatus.ACCEPTED).isCap(true).build();
        userTeamRepository.save(userTeam);
        return team;
    }

    @Override
    public List<Team> getAllActiveTeamByUser(User user) {
        List<Team> list = userTeamRepository.findAllActiveTeamByUser(user.getId());

        if (list == null || list.isEmpty()) {
            throw new RuntimeException("У вак пока нет команды");
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
        return userTeam.isCap() && userTeam.getInvitationStatus().equals(InvitationStatus.ACCEPTED);
    }

    @Override
    public boolean isCapOfUserTeam(UUID userTeamId) {
        UserTeam userTeam = userTeamRepository.findById(userTeamId).orElseThrow(() -> new RuntimeException("Пользователь не является членом данной команды"));
        return  userTeam.isCap();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTeam> getActiveInvitations(User user) {
        return userTeamRepository.findPendingInvitationsWithTeam(user);
    }

    @Override
    public void createInvitation(UUID teamId, Boolean isCap, Boolean notPlaying, User user) {
        Team team = teamRepository.findById(teamId).get();
        Optional<UserTeam> optionalUserTeam = userTeamRepository.findByUser_IdAndTeam_Id(user.getId(), teamId);
        if (optionalUserTeam.isPresent()) {
            UserTeam userTeam = optionalUserTeam.get();
            userTeam.setInvitationStatus(InvitationStatus.PENDING);
            userTeamRepository.save(userTeam);
            return;
        }
        UserTeam userTeam = UserTeam.builder().team(team).user(user).isCap(isCap).isPlaying(!notPlaying).invitationStatus(InvitationStatus.PENDING).build();

        team.getUserTeamList().add(userTeam);
        user.getUserTeamList().add(userTeam);
        userTeamRepository.save(userTeam);
    }

    @Override
    public void acceptInvitation(UUID userTeamId) {
        UserTeam userTeam = userTeamRepository.findById(userTeamId).get();
        userTeam.setInvitationStatus(InvitationStatus.ACCEPTED);
        Team team = userTeam.getTeam();

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
        team.setLogo(teamDTO.getLogo());
        team.setSport(teamDTO.getSport());
        teamRepository.save(team);
    }

    @Override
    public Team getByName(String name) {
        return teamRepository.findByName(name).orElseThrow(() -> new RuntimeException("Команды с таким именем не существует"));
    }

    @Override
    public List<Team> findTeamsWhereUserIsCap(User user) {
        List<UserTeam> userTeamList = userTeamService.findByUserId(user.getId());
        List<UserTeam> isCapList = userTeamList.stream().filter(userTeam -> userTeam.isCap()).collect(Collectors.toList());
        List<Team> teams = isCapList.stream().map(userTeam -> userTeam.getTeam()).collect(Collectors.toList());
        return teams;

    }

    @Override
    public UserTeam getUserTeamByUserAndTeam(UUID userId, UUID teamId) {
        return userTeamRepository.findByUser_IdAndTeam_Id(userId, teamId).orElseThrow(() -> new RuntimeException("Пользователь не принадлежит к данной команде"));
    }

    @Override
    public boolean isOnlyActiveCaptain(UUID teamId, UUID userId) {
        List<UserTeamDTO> members = getAllUserByTeamDTO(teamId);
        return members.stream()
                .filter(member -> member.getInvitationStatus() == InvitationStatus.ACCEPTED && member.isCap())
                .count() == 1 && isCap(teamId, userId);
    }
}
