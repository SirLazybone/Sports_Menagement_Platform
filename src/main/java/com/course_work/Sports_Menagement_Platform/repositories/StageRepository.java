package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StageRepository extends JpaRepository<Stage, UUID> {
    @Query("SELECT s " +
            "FROM Stage s " +
            "WHERE s.bestPlace = :bestPlace " +
            "AND s.worstPlace = :worstPlace " +
            "AND s.tournament.id = :tournamentId")
    Optional<Stage> findByPlaceAndTournamentId(@Param("bestPlace") int bestPlace,@Param("worstPlace") int worstPlace,@Param("tournamentId") UUID tournamentId);

    @Query("SELECT s FROM Stage s WHERE s.tournament.id = :tournamentId")
    List<Stage> findAllStageByTournamentId(@Param("tournamentId") UUID tournamentId);

    @Query("SELECT s.tournament FROM Stage s WHERE s.id = :stageId")
    Optional<Tournament> findByStageId(@Param("stageId") UUID stageId);
}
