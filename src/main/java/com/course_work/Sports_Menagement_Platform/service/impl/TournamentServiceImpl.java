package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.mapper.TournamentMapper;
import com.course_work.Sports_Menagement_Platform.repositories.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentServiceImpl implements TournamentService {
    private final TournamentRepository tournamentRepository;
    private final TeamTournamentRepository teamTournamentRepository;
    private final CityRepository cityRepository;
    private final OrgComService orgComService;
    private final TeamService teamService;
    private final UserTeamRepository userTeamRepository;
    private final StageStatusService stageStatusService;
    private final StageRepository stageRepository;
    private final GoalRepository goalRepository;
    private final MatchRepository matchRepository;
    private final TournamentMapper tournamentMapper;

    public TournamentServiceImpl(TournamentRepository tournamentRepository, OrgComService orgComService,
                               TournamentMapper tournamentMapper, CityRepository cityRepository,
                               TeamService teamService, TeamTournamentRepository teamTournamentRepository,
                                 UserTeamRepository userTeamRepository, StageStatusService stageStatusService,
                                 StageRepository stageRepository, GoalRepository goalRepository,
                                 MatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.orgComService = orgComService;
        this.tournamentMapper = tournamentMapper;
        this.cityRepository = cityRepository;
        this.teamService = teamService;
        this.teamTournamentRepository = teamTournamentRepository;
        this.userTeamRepository = userTeamRepository;
        this.stageStatusService = stageStatusService;
        this.stageRepository = stageRepository;
        this.goalRepository = goalRepository;
        this.matchRepository = matchRepository;
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
        Team team = teamService.getById(applicationDTO.getTeam());
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
    public void cancelApplication(UUID tournamentId, UUID teamId) {
        TeamTournament teamTournament = teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, teamId).orElseThrow(() -> new RuntimeException("Данная команда не подавалась на данный турнир"));
        teamTournament.setApplicationStatus(ApplicationStatus.CANCELED);
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
    public boolean isUserOrgOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComOrg(userId, orgCom.getId());
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

        Stage stage = Stage.builder()
            .worstPlace(teamTournamentIds.size())
            .bestPlace(1)
            .isPublished(false)
            .tournament(tournament)
            .build();
        stageRepository.save(stage);

        List<String> teamTournamentIdsString = teamTournamentIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        tournament.getTeamTournamentList().forEach(teamTournament -> {
            if (teamTournament.isGoToPlayOff() != teamTournamentIdsString.contains(teamTournament.getId().toString())) {
                teamTournament.setGoToPlayOff(teamTournamentIdsString.contains(teamTournament.getId().toString()));
                teamTournamentRepository.save(teamTournament);
            }
        });
    }

    @Override
    public List<RatingLineDTO> getRating(List<TeamTournament> teamTournaments) {
        if (teamTournaments == null || teamTournaments.isEmpty()) {
            throw new RuntimeException("Нет команд в данном турнире");
        }
        List<RatingLineDTO> ratingLines = new ArrayList<>();
        Optional<Stage> optionalStage = stageRepository.findByPlaceAndTournamentId(0, 0, teamTournaments.get(0).getTournament().getId());
        Stage stage;
        if (optionalStage.isPresent()) {
            stage = optionalStage.get();
        } else {
            throw new RuntimeException("Группового турнира не существует");
        }
        for (TeamTournament teamTournament : teamTournaments) {
            List<Match> matchesByTeam = matchRepository.findAllByStageIdAndTeamId(stage.getId(), teamTournament.getTeam().getId());
            // TODO: Добавить в matchesByTeam матчи дополнительного этапа
            int scoredGoals = 0;
            int missedGoals = 0;
            int winCount = 0;
            int loseCount = 0;
            int drawCount = 0;
            int points = 0;
            
            // Hockey
            int wonByBullets = 0;
            int lostByBullets = 0;
            
            // Volleyball
            int wonSets = 0;
            int lostSets = 0;
            
            for (var match : matchesByTeam) {
                if (!match.isResultPublished()) {
                    continue;
                }
                List<Goal> goalsByMatch = goalRepository.findAllByMatchId(match.getId());
                
                boolean isTechnicalDefeat = goalsByMatch.stream()
                    .anyMatch(goal -> goal.getTeam().getId().equals(teamTournament.getTeam().getId()) && goal.getTime() == -1);
                boolean isOpponentTechnicalDefeat = goalsByMatch.stream()
                    .anyMatch(goal -> !goal.getTeam().getId().equals(teamTournament.getTeam().getId()) && goal.getTime() == -1);
                
                if (isTechnicalDefeat) {
                    loseCount++;
                    continue;
                } else if (isOpponentTechnicalDefeat) {
                    winCount++;
                    switch (teamTournament.getTournament().getSport()) {
                        case FOOTBALL, HOCKEY -> points += 3;
                        case BASKETBALL, VOLLEYBALL -> points += 2;
                    }
                    continue;
                }
                
                int matchScoredGoals = 0;
                int matchMissedGoals = 0;
                int winBulletsCount = 0;
                int lostBulletsCount = 0;
                List<Integer> winGoalsBySet = Arrays.asList(0, 0, 0, 0, 0);
                List<Integer> lostGoalsBySet = Arrays.asList(0, 0, 0, 0, 0);
                
                for (var goal : goalsByMatch) {
                    if (goal.getTime() == -1) {
                        continue;
                    }
                    
                    if (goal.getTeam().getId() == teamTournament.getTeam().getId()) {
                        if (match.getStage().getTournament().getSport() == Sport.HOCKEY && goal.isPenalty()) {
                            winBulletsCount++;
                        } else if (match.getStage().getTournament().getSport() == Sport.VOLLEYBALL) {
                            winGoalsBySet.set(goal.getSet_number(), winGoalsBySet.get(goal.getSet_number()) + 1);
                        } else {
                            matchScoredGoals++;
                        }
                    } else if (match.getStage().getTournament().getSport() == Sport.HOCKEY && goal.isPenalty()) {
                        lostBulletsCount++;
                    } else if (match.getStage().getTournament().getSport() == Sport.VOLLEYBALL) {
                        lostGoalsBySet.set(goal.getSet_number(), lostGoalsBySet.get(goal.getSet_number()) + 1);
                    } else {
                        matchMissedGoals++;
                    }
                }
                scoredGoals += matchScoredGoals;
                missedGoals += matchMissedGoals;
                
                switch (teamTournament.getTournament().getSport()) {
                    case HOCKEY -> {
                        if (winBulletsCount > lostBulletsCount) {
                            wonByBullets++;
                            points += 2;
                        } else if (winBulletsCount < lostBulletsCount){
                            lostByBullets++;
                            points += 1;
                        } else if (matchScoredGoals > matchMissedGoals){
                            winCount++;
                            points += 3;
                        } else if (matchScoredGoals < matchMissedGoals){
                            loseCount++;
                        } else {
                            drawCount++;
                        }
                    }
                    case VOLLEYBALL -> {
                        int wonSetsByMatch = 0;
                        int lostSetsByMatch = 0;
                        for (int i = 0; i < 5; i++) {
                            if (winGoalsBySet.get(i) > lostGoalsBySet.get(i)) {
                                wonSets++;
                                wonSetsByMatch++;
                            } else if (winGoalsBySet.get(i) < lostGoalsBySet.get(i)) {
                                lostSets++;
                                lostSetsByMatch++;
                            }
                        }
                        if (wonSetsByMatch > lostSetsByMatch) {
                            winCount++;
                            points += 2;
                        } else if (wonSetsByMatch < lostSetsByMatch) {
                            loseCount++;
                        }
                    }
                    case FOOTBALL, BASKETBALL -> {
                        if (matchScoredGoals > matchMissedGoals) {
                            winCount++;
                        } else if (matchScoredGoals < matchMissedGoals) {
                            loseCount++;
                        } else {
                            drawCount++;
                        }
                    }
                }
            }
            
            if (teamTournament.getTournament().getSport() == Sport.FOOTBALL) {
                points = winCount * 3 + drawCount;
            } else if (teamTournament.getTournament().getSport() == Sport.BASKETBALL) {
                points = winCount * 2 + drawCount;
            }
            
            RatingLineDTO.RatingLineDTOBuilder builder = RatingLineDTO.builder()
                    .points(points)
                    .goesToPlayOff(teamTournament.isGoToPlayOff())
                    .loseCount(loseCount)
                    .drawCount(drawCount)
                    .winCount(winCount)
                    .missedGoals(missedGoals)
                    .scoredGoals(scoredGoals)
                    .teamTournamentId(teamTournament.getId())
                    .teamName(teamTournament.getTeam().getName())
                    .matchesCount(matchesByTeam.size())
                    .diffGoals(scoredGoals - missedGoals);
            
            if (teamTournament.getTournament().getSport() == Sport.HOCKEY) {
                builder.wonByBullets(wonByBullets)
                       .lostByBullets(lostByBullets);
            } else if (teamTournament.getTournament().getSport() == Sport.VOLLEYBALL) {
                builder.wonSets(wonSets)
                       .lostSets(lostSets)
                       .setsRatio(lostSets == 0 ? wonSets : (double) wonSets / lostSets)
                       .goalsRatio(missedGoals == 0 ? scoredGoals : (double) scoredGoals / missedGoals);
            }
            
            ratingLines.add(builder.build());
        }

        ratingLines.sort(Comparator
                .comparing(RatingLineDTO::getPoints).reversed()
                .thenComparing(RatingLineDTO::getDiffGoals)
                .thenComparing(RatingLineDTO::getScoredGoals));

        return ratingLines;
    }

    @Override
    public List<Tournament> search(TournamentSearchDTO tournamentSearchDTO) {
        LocalDate registrationUntil;
        if (tournamentSearchDTO.isRegistrationGoing()) registrationUntil = LocalDate.now();
        else registrationUntil = LocalDate.of(1900, 1, 1);
        List<String> strings = List.of(Sport.values()).stream().map(i -> i.toString()).collect(Collectors.toList());
        if (tournamentSearchDTO.getCities() == null) {
            if (tournamentSearchDTO.getSports() != null)
            return tournamentRepository.searchWithoutCities(tournamentSearchDTO.getName(), tournamentSearchDTO.getSports().stream().map(i -> i.toString()).collect(Collectors.toList()), tournamentSearchDTO.getTeamSizeFromInt(), tournamentSearchDTO.getTeamSizeToInt(), registrationUntil);
            else {
                return tournamentRepository.searchWithoutCities(tournamentSearchDTO.getName(), List.of(Sport.values()).stream().map(i -> i.toString()).collect(Collectors.toList()), tournamentSearchDTO.getTeamSizeFromInt(), tournamentSearchDTO.getTeamSizeToInt(), registrationUntil);

            }
        }
        else {
            if (tournamentSearchDTO.getSports() != null)

                return tournamentRepository.search(tournamentSearchDTO.getName(), tournamentSearchDTO.getCities().stream().map(i -> cityRepository.getById(i)).collect(Collectors.toList()), tournamentSearchDTO.getSports().stream().map(i -> i.toString()).collect(Collectors.toList()), tournamentSearchDTO.getTeamSizeFromInt(), tournamentSearchDTO.getTeamSizeToInt(), registrationUntil);
            else
                return tournamentRepository.search(tournamentSearchDTO.getName(), tournamentSearchDTO.getCities().stream().map(i -> cityRepository.getById(i)).collect(Collectors.toList()), List.of(Sport.values()).stream().map(i -> i.toString()).collect(Collectors.toList()), tournamentSearchDTO.getTeamSizeFromInt(), tournamentSearchDTO.getTeamSizeToInt(), registrationUntil);

        }
    }

    @Override
    public void prolongRegister(UUID tournamentId, ProlongRegDTO prolongRegDTO) {
        Tournament tournament = getById(tournamentId);
        tournament.setRegisterDeadline(prolongRegDTO.getRegisterDeadline());
        tournamentRepository.save(tournament);
    }

    @Override
    public void updateTournament(UUID tournamentId, TournamentDTO tournamentDTO) {
        Tournament tournament = getById(tournamentId);
        tournament.setName(tournamentDTO.getName());
        tournament.setLogo(tournamentDTO.getLogo());
        tournamentRepository.save(tournament);
    }

    @Override
    public List<TeamTournament> getAcceptedTeamTournament(UUID teamId) {
        List<TeamTournament> teamTournaments = teamTournamentRepository.findAllTeamTournamentByTeamId(teamId);
        return teamTournaments.stream()
                .filter(x -> x.getApplicationStatus().equals(ApplicationStatus.ACCEPTED))
                .toList();
    }

    @Override
    public List<TeamTournament> getOtherTeamTournament(UUID teamId) {
        List<TeamTournament> teamTournaments = teamTournamentRepository.findAllTeamTournamentByTeamId(teamId);
        return teamTournaments.stream()
                .filter(x -> !x.getApplicationStatus().equals(ApplicationStatus.ACCEPTED))
                .toList();
    }

    @Override
    public TeamTournament getTeamTournament(UUID teamId, UUID tournamentId) {
        return teamTournamentRepository.findByTournamentIdAndTeamId(tournamentId, teamId).orElseThrow(() -> new RuntimeException("Команда не состоит в данном турнире"));
    }

    @Override
    public boolean cancelTournament(UUID tournamentId) {
        Tournament tournament = getById(tournamentId);
        tournament.set_stopped(true);
        tournamentRepository.save(tournament);
        return true;
    }

    @Override
    public List<Tournament> findAllActive() {
        return tournamentRepository.findAllActive(LocalDate.now());
    }
}
