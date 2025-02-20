package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE m.stage = :stage AND (m.team1 = :team OR m.team2 = :team)")
    boolean existsByStageAndTeam(@Param("stage") Stage stage, @Param("team") Team team);

    @Query("SELECT m FROM Match m WHERE m.stage.id = :stageId")
    List<Match> findAllMatchesByStageId(@Param("stageId") UUID stageId);
}
