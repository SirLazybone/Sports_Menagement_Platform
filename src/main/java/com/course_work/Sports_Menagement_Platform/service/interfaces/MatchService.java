package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MatchService {
    List<Match> getAllMatches(UUID stageId);

    void createMatches(List<MatchDTO> matchDTO);

    Match getById(UUID matchId);

    Map<UUID, List<User>> getTeamMembersMap(Team team1, Team team2);

    Map<UUID, List<Match>> getMatchesByStagesMap(List<Stage> stages);

    void assignSlotToMatch(UUID slotId, UUID matchId);
}
