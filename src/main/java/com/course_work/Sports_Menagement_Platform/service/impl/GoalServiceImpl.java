package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Goal;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;
import com.course_work.Sports_Menagement_Platform.repositories.GoalRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GoalService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.stereotype.Service;

@Service
public class GoalServiceImpl implements GoalService {
    private final GoalRepository goalRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final MatchService matchService;
    public GoalServiceImpl(GoalRepository goalRepository, TeamService teamService,
                           UserService userService, MatchService matchService) {
        this.goalRepository = goalRepository;
        this.teamService = teamService;
        this.userService = userService;
        this.matchService = matchService;
    }

    @Override
    public void addGoal(GoalDTO goalDTO) {
        Goal goal = new Goal();
        goal.setPenalty(goalDTO.isPenalty());
        goal.setTime(goalDTO.getTime());
        goal.setPoints(goalDTO.getPoints());
        goal.setSet_number(goalDTO.getSetNumber());

        Team team = teamService.getById(goalDTO.getTeamId());
        User user = userService.getById(goalDTO.getUserId());
        Match match = matchService.getById(goalDTO.getMatchId());

        goal.setTeam(team);
        goal.setUser(user);
        goal.setMatch(match);

        goalRepository.save(goal);
    }
}
