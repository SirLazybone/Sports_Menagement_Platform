package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.dto.TeamTournamentDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;
import com.course_work.Sports_Menagement_Platform.mapper.TournamentMapper;
import com.course_work.Sports_Menagement_Platform.repositories.CityRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamTournamentRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TournamentRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService {
    private final TournamentRepository tournamentRepository;
    private final TeamTournamentRepository teamTournamentRepository;
    private final CityRepository cityRepository;
    private final OrgComService orgComService;
    private final TeamService teamService;
    private final TournamentMapper tournamentMapper;


    public TournamentServiceImpl(TournamentRepository tournamentRepository, OrgComService orgComService,
                                 TournamentMapper tournamentMapper, CityRepository cityRepository,
                                 TeamService teamService, TeamTournamentRepository teamTournamentRepository) {
        this.tournamentRepository = tournamentRepository;
        this.orgComService = orgComService;
        this.tournamentMapper = tournamentMapper;
        this.cityRepository = cityRepository;
        this.teamService = teamService;
        this.teamTournamentRepository = teamTournamentRepository;
    }
    @Override
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    @Override
    public void createTournament(TournamentDTO tournamentDTO, User user) {
        UserOrgCom userOrgCom = orgComService.getUserOrgComChief(tournamentDTO.getOrgComName(), user.getId());
        City city = cityRepository.findByName(tournamentDTO.getCityName()).orElseThrow(() -> new RuntimeException("No such city"));
        Tournament tournament = tournamentMapper.DTOToEntity(tournamentDTO); // Add UserOrgCom and City
        tournament.setUserOrgCom(userOrgCom);
        tournament.setCity(city);

        tournamentRepository.save(tournament);

//        if (tournamentDTO.isClassicScheme()) {
//            stageService.createClassicScheme(tournament);
//        }
    }

    @Override
    public Tournament getById(UUID id) {
        return tournamentRepository.findById(id).orElseThrow(() -> new RuntimeException("No such tournament"));
    }

    @Override
    public TournamentDTO getDTOById(UUID id) {
        Tournament tournament = tournamentRepository.findById(id).orElseThrow(() -> new RuntimeException("No such tournament"));
        return tournamentMapper.EntityToDTO(tournament);
    }

    @Override
    public void createApplication(ApplicationDTO applicationDTO, UUID tournamentId, UUID userId) {
        Team team = teamService.getByName(applicationDTO.getTeamName());
        if (!teamService.isCap(team.getId(), userId)) {
            throw new RuntimeException("Только капитан может подавать заявки на турнир");
        }
        Tournament tournament = tournamentRepository.findById(tournamentId).get();
        isTeamCorrForTournament(tournament, team);

        Optional<TeamTournament> teamTournamentOptional = teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, applicationDTO.getTeamId());
        if (teamTournamentOptional.isPresent()) {
            throw new RuntimeException("Команда уже подавалась на турнир");
        }

        TeamTournament teamTournament = TeamTournament.builder()
                .tournament(tournament)
                .team(team)
                .applicationStatus(ApplicationStatus.PENDING).build();

        tournament.getTeamTournamentList().add(teamTournament);
        team.getTeamTournamentList().add(teamTournament);

        teamTournamentRepository.save(teamTournament);
    }
    private void isTeamCorrForTournament(Tournament tournament, Team team) {
        if (tournament.getSport() != team.getSport()) {
            throw new RuntimeException("Спорт турнира отличается от вида спорта команды");
        }
        if (tournament.getMinMembers() > team.getUserTeamList().stream().filter(i -> i.isPlaying()).collect(Collectors.toList()).size()) {
            throw new RuntimeException("Количество участников команды меньше, чем допускает регламент турнира");
        }
        if (tournament.getRegisterDeadline().isBefore(LocalDate.now())) {
            throw new RuntimeException("Время подачи заявок прошло");
        }
    }

    @Override
    public List<ApplicationDTO> getCurrAppl(UUID tournamentId) {
        List<Team> teams =  teamTournamentRepository.findAllTeamsByTournamentIdAndStatus(tournamentId, ApplicationStatus.PENDING);
        return teams.stream().map(x -> new ApplicationDTO(x.getName(), x.getId())).toList();
    }

    @Override
    public void approveApplication(UUID tournamentId, UUID teamId) {
        TeamTournament teamTournament = teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, teamId).orElseThrow(() -> new RuntimeException("Данная команда не подавалась на данный турнир"));
        teamTournament.setApplicationStatus(ApplicationStatus.ACCEPTED);
        teamTournamentRepository.save(teamTournament);
    }

    @Override
    public void rejectApplication(UUID tournamentId, UUID teamId) {
        TeamTournament teamTournament = teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, teamId).orElseThrow(() -> new RuntimeException("Данная команда не подавалась на данный турнир"));
        teamTournament.setApplicationStatus(ApplicationStatus.DECLINED);
        teamTournamentRepository.save(teamTournament);
    }

    @Override
    public List<ApplicationDTO> getCurrParticipants(UUID tournamentId) {
        List<Team> teams = teamTournamentRepository.findAllTeamsByTournamentIdAndStatus(tournamentId, ApplicationStatus.ACCEPTED);
        return teams.stream().map(x -> new ApplicationDTO(x.getName(), x.getId())).toList();
    }

    @Override
    public boolean isUserChiefOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComChief(userId, orgCom.getId());
    }

    @Override
    public boolean isUserRefOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComRef(userId, orgCom.getId());
    }

    @Override
    public boolean isUserMemberOfOrgCom(UUID userId, OrgCom orgCom) {
        UserOrgCom userOrgCom = orgComService.getUserOrgComByUserAndOrgCom(userId, orgCom.getId());
        return userOrgCom.getInvitationStatus().equals(InvitationStatus.ACCEPTED);
    }

    @Override
    public List<Tournament> getAllTournamentsOfOrgCom(UUID orgcomId) {
        return tournamentRepository.findAllByOrgComId(orgcomId);
    }

    @Override
    public List<TeamTournamentDTO> getTournamentsByTeam(UUID teamId) {
        List<TeamTournament> teamTournaments = teamTournamentRepository.findAllTeamTournamentByTeamId(teamId);
        return teamTournaments.stream().map(x -> new TeamTournamentDTO(x.getTournament().getName(), x.getTournament().getId(), x.getApplicationStatus())).toList();
    }

    @Override
    public List<Team> getAllTeamsByTournamentId(UUID tournamentId) {
        return teamTournamentRepository.findAllTeamsByTournamentId(tournamentId);
    }

    @Override
    public List<Tournament> getAllTournamentsByUserOrgComId(UUID userOrgComId) {
        return tournamentRepository.findAllTournamentsByUserOrgComId(userOrgComId);
    }

}
