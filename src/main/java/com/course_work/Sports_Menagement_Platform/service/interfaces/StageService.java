package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;

import java.util.List;
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
}
