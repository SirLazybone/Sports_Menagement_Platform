package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import org.springframework.data.util.Pair;

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

    Map<Group, List<Match>> createGroupMatchIfNotCreated(UUID stageId);

    void setSlots(UUID stageId, Map<UUID, UUID> assignments);

    void setSlotsForPlayOff(UUID stageId, Map<Pair<UUID, UUID>, UUID> assignments, List<Pair<UUID, UUID>> assigmentsWithNoSlot);

    List<Match> getAllByStageAndTeam(Stage stage, Team team);
}
