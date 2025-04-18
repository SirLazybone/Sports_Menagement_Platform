package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.Group;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StageService {
    void createStage(StageCreationDTO stageDTO, UUID tournamentId);

    List<Stage> getStagesByTournament(UUID tournamentId);

    List<Team> getTeamsByStageId(UUID stageId);

    Stage getStageById(UUID stageId);

    Tournament getTournamentByStage(UUID stageId);

    void publishStage(UUID stageId);

    void createClassicScheme(Tournament tournament);

    String getStageName(int bestPlace, int worstPlace);

    // Group stage methods
    void createGroups(GroupsDTO groupsDTO);
    
    List<Group> getGroupsByStage(UUID stageId);
    
    void addTeamsToGroup(GroupDTO groupDTO);
    
    boolean isGroupStage(UUID stageId);

    Stage getStageByGroup(UUID groupId);
    void removeTeamFromGroup(UUID groupId, UUID teamId);
}
