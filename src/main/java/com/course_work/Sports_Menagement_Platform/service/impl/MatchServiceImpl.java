package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.AdditionalMatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.repositories.StageRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final StageService stageService;
    private final SlotService slotService;
    private final StageRepository stageRepository;


    public MatchServiceImpl(MatchRepository matchRepository, TeamService teamService,
                            UserService userService, StageService stageService,
                            SlotService slotService, GroupService groupService, StageRepository stageRepository, AfterMatchPenaltyService afterMatchPenaltyService) {
        this.matchRepository = matchRepository;
        this.teamService = teamService;
        this.userService = userService;
        this.stageService = stageService;
        this.slotService = slotService;
        this.stageRepository = stageRepository;
    }

    @Override
    public Match getById(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("Такого матча нет"));
    }

    @Override
    public List<Match> getAllMatches(UUID stageId) {
        return matchRepository.findAllMatchesByStageId(stageId);
    }

    @Override
    public void createMatches(List<MatchDTO> matchDTOs) {
        for (MatchDTO matchDTO : matchDTOs) {
            if (matchDTO.getTeam1Id() == null || matchDTO.getTeam2Id() == null) {
                continue;
            }
            Stage stage = stageService.getStageById(matchDTO.getStageId());
            Team team1 = teamService.getById(matchDTO.getTeam1Id());
            Team team2 = teamService.getById(matchDTO.getTeam2Id());
            boolean team1Exists = matchRepository.existsByStageAndTeam(stage, team1);
            boolean team2Exists = matchRepository.existsByStageAndTeam(stage, team2);

            if (team1Exists || team2Exists) {
                throw new RuntimeException("Одна из команд уже участвует в матче на этом этапе!");
            }

            Match match = Match.builder()
                    .stage(stage)
                    .team1(team1)
                    .team2(team2)
                    .isResultPublished(false)
                    .build();
            try {
                Slot slot = slotService.getById(matchDTO.getSlotId());
                match.setSlot(slot);
            } catch (RuntimeException ignored) {}


            matchRepository.save(match);
        }
    }

    @Override
    public Map<UUID, List<User>> getTeamMembersMap(Team team1, Team team2) {
        Map<UUID, List<User>> players = new HashMap<>();
        players.put(team1.getId(), teamService.getAllUserByTeam(team1.getId()));
        players.put(team2.getId(), teamService.getAllUserByTeam(team2.getId()));
        return players;
    }

    @Override
    public Map<UUID, List<Match>> getMatchesByStagesMap(List<Stage> stages) {
        Map<UUID, List<Match>> matches = new HashMap<>();
        for (Stage stage : stages) {
            matches.put(stage.getId() ,matchRepository.findAllMatchesByStageId(stage.getId()));
        }
        return matches;
    }

    @Override
    public void assignSlotToMatch(UUID slotId, UUID matchId) {
        Match match = getById(matchId);
        Slot slot = slotService.getById(slotId);
        match.setSlot(slot);
        matchRepository.save(match);
    }


    @Override
    public Map<Group, List<Match>> createGroupMatchIfNotCreated(UUID stageId) {
        Stage stage = stageService.getStageById(stageId);
        List<Stage> stageList = new ArrayList<>();
        stageList.add(stage);
        List<Match> matches = getMatchesByStagesMap(stageList).get(stageId);
        Map<Group, List<Match>> result = new HashMap<>();


        if (matches.isEmpty()) {
             List<Group> groups = stageService.getGroupsByStage(stageId);
             for (Group group : groups) {
                 result.put(group, new ArrayList<>());
                 for (Team team1 : group.getTeams()) {
                     for (Team team2 : group.getTeams()) {
                        if (!team1.getId().equals(team2.getId())) {
                            Match match = Match.builder().team1(team1).team2(team2).isResultPublished(false).stage(stage).build();
                            match = matchRepository.save(match);
                            result.get(group).add(match);
                        }
                     }
                 }
             }
        }
        else {
            List<Group> groups = stageService.getGroupsByStage(stageId);
            for (Group group : groups) {
                List<String> teamIds = group.getTeams().stream().map(i -> i.getId().toString()).collect(Collectors.toList());
                List<Match> groupMatches = matches.stream().filter(i -> teamIds.contains(i.getTeam1().getId().toString())).toList();
                result.put(group, groupMatches);

            }


        }
        return result;


    }

    @Override
    public void setSlots(UUID stageId, Map<UUID, UUID> assignments) {
        // TODO: проверки
        assignments.forEach((matchId, slotId) -> {
            Match match = matchRepository.findById(matchId).get();
            match.setSlot(slotService.getById(slotId));
            matchRepository.save(match);
            }
        );
    }

    @Override
    public void setSlotsForPlayOff(UUID stageId, Map<Pair<UUID, UUID>, UUID> assignments, List<Pair<UUID, UUID>> assigmentsWithNoSlot) {

        Stage stage = stageService.getStageById(stageId);
        List<Stage> stageList = new ArrayList<>();
        stageList.add(stage);
        List<Match> matches = getMatchesByStagesMap(stageList).get(stageId);
        matchRepository.deleteAll(matches);

        assignments.forEach((teams, slotId) -> {
                    Match match = Match.builder().slot(slotService.getById(slotId)).
                            team1(teamService.getById(teams.getFirst())).team2(teamService.getById(teams.getSecond())).
                            isResultPublished(false).stage(stage).build();
                    matchRepository.save(match);

                }
        );

        assigmentsWithNoSlot.forEach((teams) -> {
                    Match match = Match.builder().
                            team1(teamService.getById(teams.getFirst())).team2(teamService.getById(teams.getSecond())).
                            isResultPublished(false).stage(stage).build();
                    matchRepository.save(match);

                }
        );


    }

    @Override
    public List<Match> getAllByStageAndTeam(Stage stage, Team team) {
        return matchRepository.findAllByStageIdAndTeamId(stage.getId(), team.getId());
    }

    @Override
    public List<Match> getFinishedAdditionalMatches(UUID tournamentId) {
        return stageService.getAdditionalStages(tournamentId).stream().map(i -> i.getMatches()).flatMap(List::stream).filter(match -> match.isResultPublished()).collect(Collectors.toList());
    }

    @Override
    public List<Match> getPublishedAdditionalMatches(UUID tournamentId) {
        return stageService.getAdditionalStages(tournamentId).stream().filter(stage -> stage.isPublished()).map(i -> i.getMatches()).flatMap(List::stream).filter(match -> !match.isResultPublished()).collect(Collectors.toList());
    }

    @Override
    public List<Match> getAdditionalMatchesInWork(UUID tournamentId) {
        return stageService.getAdditionalStages(tournamentId).stream().filter(stage -> !stage.isPublished()).map(i -> i.getMatches()).flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public Match createAdditionalMatch(UUID tournamentId, AdditionalMatchDTO additionalMatchDTO) {
        Stage stage = stageService.createStageForAdditionalMatch(tournamentId);
        Match match = Match.builder().stage(stage).team1(teamService.getById(additionalMatchDTO.getTeam1())).team2(teamService.getById(additionalMatchDTO.getTeam2())).isResultPublished(false).
                slot(slotService.getById(additionalMatchDTO.getSlot())).build();
        return matchRepository.save(match);
    }

    @Override
    public Match publishResult(UUID matchId) {
        Match match = matchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("Нет такого матча"));
        match.setResultPublished(true);
        if (match.getStage().getBestPlace() > 0 && match.getStage().getMatches().stream().allMatch(match1 -> match1.isResultPublished())) {
            int bestPlace = match.getStage().getBestPlace();
            int worstPlace = match.getStage().getWorstPlace();
            int worstNew = (bestPlace + worstPlace) / 2;
            int bestNew = worstNew + 1;
            if (worstNew == bestPlace) {
                Stage stage1 = Stage.builder().isPublished(false).bestPlace(bestPlace).worstPlace(worstNew).tournament(match.getStage().getTournament()).build();
                Stage stage2 = Stage.builder().isPublished(false).bestPlace(bestNew).worstPlace(worstPlace).tournament(match.getStage().getTournament()).build();
                stageRepository.save(stage1);
                stageRepository.save(stage2);
            }
        }

        return matchRepository.save(match);
    }


}
