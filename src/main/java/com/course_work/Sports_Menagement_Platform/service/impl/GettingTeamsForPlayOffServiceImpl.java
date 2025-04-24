package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.AfterMatchPenaltyService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GettingTeamsForPlayOffService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GoalService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class GettingTeamsForPlayOffServiceImpl implements GettingTeamsForPlayOffService {
    private final StageService stageService;
    private final GoalService goalService;
    private final AfterMatchPenaltyService afterMatchPenaltyService;

    public GettingTeamsForPlayOffServiceImpl(StageService stageService, GoalService goalService, AfterMatchPenaltyService afterMatchPenaltyService) {
        this.stageService = stageService;
        this.goalService = goalService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
    }


    @Override
    public List<Team> getTeamsForPlayOffStage(Stage stage) {
        int maxPlayOffPlace = Collections.max(stage.getTournament().getStages().stream().filter(i -> i.getBestPlace() > 0).map(i -> i.getWorstPlace()).collect(Collectors.toList()));
        if (stage.getWorstPlace() == maxPlayOffPlace) {
            return stage.getTournament().getTeamTournamentList().stream().filter(i -> i.isGoToPlayOff()).map(i -> i.getTeam()).collect(Collectors.toList());
        }
        else {
            Stage prevoiusStage =  stageService.getPrevious(stage);
            List<Match> previousStageMatches = prevoiusStage.getMatches();
            if (stage.getBestPlace() == prevoiusStage.getWorstPlace()) {
                return previousStageMatches.stream().map(match -> getWinningTeam(match)).collect(Collectors.toList());
            }
            else {
                return previousStageMatches.stream().map(match -> getLosingTeam(match)).collect(Collectors.toList());

            }
        }
    }


    @Override
    public Team getWinningTeam(Match match) {
        List<Goal> goals = goalService.getGoalsByMatch(match.getId());

        if (match.getStage().getTournament().getSport() != Sport.VOLLEYBALL) {
            int team1GoalsCount = goals.stream().filter(goal -> goal.getTeam().getId().equals(match.getTeam1().getId())).collect(Collectors.toList()).size();
            if (team1GoalsCount > goals.size() - team1GoalsCount) {
                return match.getTeam1();
            } else if (team1GoalsCount < goals.size() - team1GoalsCount) {
                return match.getTeam2();
            } else {
                List<AfterMatchPenalty> afterMatchPenalties = afterMatchPenaltyService.getPenaltiesByMatch(match.getId());
                int team1successCount = afterMatchPenalties.stream().filter(i -> i.getTeam().getId().equals(match.getTeam1().getId()) && i.isScored()).collect(Collectors.toList()).size();
                int team2successCount = afterMatchPenalties.stream().filter(i -> i.getTeam().getId().equals(match.getTeam2().getId()) && i.isScored()).collect(Collectors.toList()).size();
                if (team1successCount > team2successCount) return match.getTeam1();
                else return match.getTeam2();
            }
        } else {
            Map<Integer, Integer> team1sets = new HashMap<>();
            Map<Integer, Integer> team2sets = new HashMap<>();
            goals.forEach(
                    goal -> {
                        int set = goal.getSet_number();
                        if (goal.getTeam().getId().equals(match.getTeam1().getId())) {
                            team1sets.put(set, team1sets.getOrDefault(set, 0) + 1);
                        } else {
                            team2sets.put(set, team2sets.getOrDefault(set, 0) + 1);
                        }
                    }
            );
            Set<Integer> sets = new HashSet<>(team1sets.keySet());
            sets.addAll(team2sets.keySet());
            AtomicInteger team1Count = new AtomicInteger();
            sets.forEach(set ->
                    {
                        if (team1sets.getOrDefault(set, 0) > team2sets.getOrDefault(set, 0)) team1Count.getAndIncrement();
                    }
            );
            if (sets.size() - team1Count.get() > team1Count.get()) return match.getTeam2();
            else return match.getTeam1();

        }

    }


    @Override
    public Team getLosingTeam(Match match) {
        if (getWinningTeam(match).getId().equals(match.getTeam1())) return match.getTeam2();
        return match.getTeam1();
    }
}
