package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;

import java.util.List;
import java.util.UUID;

public interface TournamentService {
    List<Tournament> getAllTournaments();

    Tournament createTournament(TournamentDTO tournamentDTO, User user, UUID orgComId);

    Tournament getById(UUID id);

    TournamentDTO getDTOById(UUID id);

    void createApplication(ApplicationDTO applicationDTO, UUID tournamentId, UUID userId);

    List<TeamTournament> getCurrAppl(UUID id);

    void approveApplication(UUID tournamentId, UUID teamId);

    void rejectApplication(UUID tournamentId, UUID teamId);

    void cancelApplication(UUID tournamentId, UUID teamId);

    List<ApplicationDTO> getCurrParticipants(UUID tournamentId);

    List<TeamTournament> getCurrentParticipants(UUID tournamentId);

    boolean isUserChiefOfTournament(UUID userId, UUID tournamentId);

    boolean isUserRefOfTournament(UUID userId, UUID tournamentId);

    List<TeamTournamentDTO> getTournamentsByTeam(UUID teamId);

    List<Team> getAllTeamsByTournamentId(UUID tournamentId);

    List<Tournament> getAllTournamentsByUserOrgComId(UUID userOrgComId);

    boolean isUserOrgOfTournament(UUID userId, UUID tournamentId);

    boolean isUserMemberOfOrgCom(UUID userId, OrgCom orgCom);

    List<Tournament> getAllTournamentsOfOrgCom(UUID id);

    void updatePlayOffTeams(UUID tournamentId, List<UUID> teamTournamentIds);

    List<RatingLineDTO> getRating(List<TeamTournament> teamTournaments);

    List<Tournament> search(TournamentSearchDTO tournamentSearchDTO);

    void prolongRegister(UUID tournamentId, ProlongRegDTO prolongRegDTO);

    void updateTournament(UUID tournamentId, TournamentDTO tournamentDTO);

    List<TeamTournament> getAcceptedTeamTournament(UUID teamId);

    List<TeamTournament> getOtherTeamTournament(UUID teamId);

    TeamTournament getTeamTournament(UUID teamId, UUID tournamentId);

    public boolean cancelTournament(UUID tournamentId);
    
    List<Tournament> findAllActive();
}
