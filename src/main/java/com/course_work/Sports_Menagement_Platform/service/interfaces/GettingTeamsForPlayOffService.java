package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;

import java.util.List;

public interface GettingTeamsForPlayOffService {

    List<Team> getTeamsForPlayOffStage(Stage stage);

    Team getWinningTeam(Match match);

    Team getLosingTeam(Match match);
}
