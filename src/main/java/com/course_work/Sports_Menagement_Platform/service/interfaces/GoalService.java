package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Goal;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;

import java.util.List;
import java.util.UUID;

public interface GoalService {
    void addGoal(GoalDTO goalDTO);
    List<Goal> getGoalsByMatch(UUID matchId);
}
