package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.TeamTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamTournamentRepository extends JpaRepository<TeamTournament, UUID> {
//    @Query("SELECT tt.team " +
//            "FROM TeamTournament tt " +
//            "WHERE tt.tournament.id = :tournamentId " +
//            "AND tt.applicationStatus = com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus.PENDING")
//    List<Team> findAllPendingTeamsByTournamentId(@Param("tournamentId") UUID tournamentId);
//
//    @Query("SELECT tt.team " +
//            "FROM TeamTournament tt " +
//            "WHERE tt.tournament.id = :tournamentId " +
//            "AND tt.applicationStatus = com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus.ACCEPTED")
//    List<Team> findAllAcceptedTeamsByTournamentId(@Param("tournamentId") UUID tournamentId);

    @Query("SELECT tt.team " +
            "FROM TeamTournament tt " +
            "WHERE tt.tournament.id = :tournamentId " +
            "AND tt.applicationStatus = :status")
    List<Team> findAllTeamsByTournamentIdAndStatus(@Param("tournamentId") UUID tournamentId, @Param("status")ApplicationStatus status);

    Optional<TeamTournament> findByTournamentIdAndTeamId(UUID tournamentId, UUID teamId);

    @Query("SELECT tt " +
            "FROM TeamTournament tt " +
            "WHERE tt.team.id = :teamId")
    List<TeamTournament> findAllTeamTournamentByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT tt.team FROM TeamTournament tt WHERE tt.tournament.id = :tournamentId")
    List<Team> findAllTeamsByTournamentId(@Param("tournamentId") UUID tournamentId);


    @Query("SELECT tt FROM TeamTournament tt WHERE tt.tournament.id = :tournamentId")
    List<TeamTournament> findAllTeamTournamentByTournamentId(UUID tournamentId);
}
