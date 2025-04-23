package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.TeamTournament;
import com.course_work.Sports_Menagement_Platform.repositories.GroupRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageStatusService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StageStatusServiceImpl implements StageStatusService {
    private final GroupRepository groupRepository;

    public StageStatusServiceImpl(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }


    @Override
    public StageStatus getStageStatus(Stage stage) {
        List<Match> matches = stage.getMatches();
        if (matches.stream().allMatch(i -> i.isResultPublished()) && matches.size() > 0) {
            return StageStatus.FINISHED;
        }
        if (stage.isPublished()) return StageStatus.SCHEDULE_PUBLISHED;
        if (stage.getWorstPlace() == 0 && stage.getWorstPlace() == 0) { // групповой этап


            List<List<Team>> teamsByGroups = groupRepository.findByStageId(stage.getId()).stream().map(i -> i.getTeams()).collect(Collectors.toList());
            List<String> teamsInGroups = teamsByGroups.stream()
                    .flatMap(List::stream).map(i -> i.getId().toString())
                    .collect(Collectors.toList());
            List<TeamTournament> tournamentTeams = stage.getTournament().getTeamTournamentList().stream().filter(i -> i.getApplicationStatus() == ApplicationStatus.ACCEPTED).collect(Collectors.toList());
            if (tournamentTeams.isEmpty()) return StageStatus.TEAMS_UNKNOWN;
            List<String> teamsInTournament = tournamentTeams.stream().map(i -> i.getTeam().getId().toString()).collect(Collectors.toList());
            if (teamsInTournament.size() == teamsInGroups.size()
                    && teamsInTournament.containsAll(teamsInGroups)) {
                return StageStatus.TEAMS_KNOWN;
            }
            else return StageStatus.TEAMS_UNKNOWN;
        }
        if (stage.getWorstPlace() < 0) {  // Доп. матчи
            return StageStatus.TEAMS_KNOWN;
        }
        // этапы плей оффа
        else return StageStatus.TEAMS_KNOWN; // TODO: этап создается только после выявления команд
    }

}
