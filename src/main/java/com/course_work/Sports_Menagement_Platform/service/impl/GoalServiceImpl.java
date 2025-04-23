package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;
import com.course_work.Sports_Menagement_Platform.repositories.GoalRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GoalService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final MatchService matchService;
    private final TeamService teamService;
    private final UserService userService;

    public GoalServiceImpl(GoalRepository goalRepository, MatchService matchService,
                           TeamService teamService, UserService userService) {
        this.goalRepository = goalRepository;
        this.matchService = matchService;
        this.teamService = teamService;
        this.userService = userService;
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
