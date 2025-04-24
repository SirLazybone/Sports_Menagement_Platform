package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
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

    int getTeamsCountInPlayOff(Tournament tournament);

    // Group stage methods
    void createGroups(GroupsDTO groupsDTO);

    Stage createGroupStageIfNotExists(UUID tournamentId);

    List<Group> getGroupsByStage(UUID stageId);
    
    void addTeamsToGroup(GroupDTO groupDTO);
    
    boolean isGroupStage(UUID stageId);

    Stage getStageByGroup(UUID groupId);
    void removeTeamFromGroup(UUID groupId, UUID teamId);

    Stage getGroupStage(UUID tournamentId);


    StageStatus getStageStatus(Stage stage);

    Stage getPrevious(Stage stage);


    List<Stage> getAdditionalStages(UUID tournamentId);

    Stage createStageForAdditionalMatch(UUID tournamentId);
}
