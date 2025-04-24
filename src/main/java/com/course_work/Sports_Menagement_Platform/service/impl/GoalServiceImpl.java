package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;
import com.course_work.Sports_Menagement_Platform.repositories.GoalRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final AfterMatchPenaltyService afterMatchPenaltyService;

    private final MatchService matchService;


    public GoalServiceImpl(GoalRepository goalRepository, MatchService matchService,
                           TeamService teamService, UserService userService, AfterMatchPenaltyService afterMatchPenaltyService, MatchService matchService1) {
        this.goalRepository = goalRepository;
        this.teamService = teamService;
        this.userService = userService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
        this.matchService = matchService1;
    }

    @Override
    public void addGoal(GoalDTO goalDTO) {
        Match match = matchService.getById(goalDTO.getMatchId());
        Team team = teamService.getById(goalDTO.getTeamId());
        User player = userService.getById(goalDTO.getPlayerId());

        Goal goal = Goal.builder()
                .match(match)
                .team(team)
                .player(player)
                .time(goalDTO.getTime())
                .isPenalty(goalDTO.isPenalty())
                .build();

        goalRepository.save(goal);
    }

    @Override
    public List<Goal> getGoalsByMatch(UUID matchId) {
        return goalRepository.findAllByMatchId(matchId);
    }



}
