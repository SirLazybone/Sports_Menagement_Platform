package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.dto.TeamTournamentDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;
import com.course_work.Sports_Menagement_Platform.mapper.TournamentMapper;
import com.course_work.Sports_Menagement_Platform.repositories.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;
    private final StageStatusService stageStatusService;

    private final StageRepository stageRepository;
    private final TournamentMapper tournamentMapper;


    public TournamentServiceImpl(TournamentRepository tournamentRepository, OrgComService orgComService,
                                 TournamentMapper tournamentMapper, CityRepository cityRepository,
                                 TeamService teamService, TeamTournamentRepository teamTournamentRepository, TeamRepository teamRepository, UserTeamRepository userTeamRepository, StageStatusService stageStatusService, StageRepository stageRepository) {
        this.tournamentRepository = tournamentRepository;
        this.orgComService = orgComService;
        this.tournamentMapper = tournamentMapper;
        this.cityRepository = cityRepository;
        this.teamService = teamService;
        this.teamTournamentRepository = teamTournamentRepository;
        this.teamRepository = teamRepository;
        this.userTeamRepository = userTeamRepository;
        this.stageStatusService = stageStatusService;
        this.stageRepository = stageRepository;
    }
    @Override
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    @Override
    public Tournament createTournament(TournamentDTO tournamentDTO, User user, UUID orgComId) {
        UserOrgCom userOrgCom = orgComService.getUserOrgComChief(orgComId, user.getId());
        City city = cityRepository.findByName(tournamentDTO.getCityName()).orElseThrow(() -> new RuntimeException("No such city"));
        Tournament tournament = tournamentMapper.DTOToEntity(tournamentDTO); // Add UserOrgCom and City
        tournament.setUserOrgCom(userOrgCom);
        tournament.setCity(city);

        tournamentRepository.save(tournament);

        return tournament;
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
    public void createApplication(ApplicationDTO applicationDTO, UUID tournamentId, UUID userId) {

        Team team = teamRepository.findById(applicationDTO.getTeam()).orElseThrow(() -> new RuntimeException("Команда не надена"));
        UserTeam userTeam = userTeamRepository.findByUser_IdAndTeam_Id(userId, team.getId()).orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));


        if (!userTeam.isCap()) {
            throw new RuntimeException("Только капитан может подавать заявки на турнир");
        }
        Tournament tournament = tournamentRepository.findById(tournamentId).get();
        isTeamCorrForTournament(tournament, team);

        Optional<TeamTournament> teamTournamentOptional = teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, applicationDTO.getTeam());
        if (teamTournamentOptional.isPresent()) {
            throw new RuntimeException("Команда уже подавалась на турнир");
        }

        TeamTournament teamTournament = TeamTournament.builder()
                .tournament(tournament)
                .team(team).goToPlayOff(false)
                .applicationStatus(ApplicationStatus.PENDING).build();

        tournament.getTeamTournamentList().add(teamTournament);
        team.getTeamTournamentList().add(teamTournament);

        teamTournamentRepository.save(teamTournament);
    }

    @Override
    public List<TeamTournament> getCurrAppl(UUID tournamentId) {
        List<TeamTournament> teams =  teamTournamentRepository.findAllTeamTournamentByTournamentId(tournamentId);
        return teams;
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
        return teams.stream().map(x -> new ApplicationDTO(x.getId())).toList();
    }

    @Override
    public List<TeamTournament> getCurrentParticipants(UUID tournamentId) {
        return tournamentRepository.findById(tournamentId).get().getTeamTournamentList().stream().filter(i -> i.getApplicationStatus() == ApplicationStatus.ACCEPTED).toList();
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


    @Override
    public void updatePlayOffTeams(UUID tournamentId, List<UUID> teamTournamentIds) {
        if ((teamTournamentIds.size() & (teamTournamentIds.size() - 1)) != 0) {
            throw new RuntimeException("Количество команд в плей-оффе должно быть степенью двойки");
        }
        List<Stage> playOffStages = tournamentRepository.getById(tournamentId).getStages().stream().filter(i -> i.getBestPlace() > 0).collect(Collectors.toList());
        List<StageStatus> playOffStagesStatuses = playOffStages.stream().map(i -> stageStatusService.getStageStatus(i)).collect(Collectors.toList());
        if (playOffStagesStatuses.contains(StageStatus.FINISHED) || playOffStagesStatuses.contains(StageStatus.SCHEDULE_PUBLISHED)) {
            throw new RuntimeException("Действие недоступно");
        }
        stageRepository.deleteAll(playOffStages);
        Tournament tournament = getById(tournamentId);

        Stage.builder().worstPlace(teamTournamentIds.size()).bestPlace(1).isPublished(false).tournament(tournament).build();
        List<String> teamTournamentIdsString = teamTournamentIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        tournament.getTeamTournamentList().forEach( teamTournament ->{

                if (teamTournament.isGoToPlayOff() != teamTournamentIdsString.contains(teamTournament.getId().toString())) {
                    teamTournament.setGoToPlayOff(teamTournamentIdsString.contains(teamTournament.getId().toString()));
                    teamTournamentRepository.save(teamTournament);
                }

                }
        );

    }
}
