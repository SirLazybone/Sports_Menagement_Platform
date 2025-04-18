package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.TeamTournamentDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;

import java.util.List;
import java.util.UUID;

public interface TournamentService {
    List<Tournament> getAllTournaments();

    void createTournament(TournamentDTO tournamentDTO, User user);

    Tournament getById(UUID id);

    TournamentDTO getDTOById(UUID id);

    void createApplication(ApplicationDTO applicationDTO, UUID tournamentId, UUID userId);

    List<ApplicationDTO> getCurrAppl(UUID id);

    void approveApplication(UUID tournamentId, UUID teamId);

    void rejectApplication(UUID tournamentId, UUID teamId);

    List<ApplicationDTO> getCurrParticipants(UUID tournamentId);

    boolean isUserChiefOfTournament(UUID userId, UUID tournamentId);

    List<TeamTournamentDTO> getTournamentsByTeam(UUID teamId);


    List<Team> getAllTeamsByTournamentId(UUID tournamentId);

    List<Tournament> getAllTournamentsByUserOrgComId(UUID userOrgComId);
}
