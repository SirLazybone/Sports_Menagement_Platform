package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.AfterMatchPenalty;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AfterMatchPenaltyRepository extends JpaRepository<AfterMatchPenalty, UUID> {
    @Query("SELECT a FROM AfterMatchPenalty a WHERE a.match.id = :matchId")
    List<AfterMatchPenalty> findAllByMatchId(@Param("matchId") UUID matchId);
    
    @Query("SELECT COUNT(a) > 0 FROM AfterMatchPenalty a WHERE a.match = :match")
    boolean existsByMatch(@Param("match") Match match);
}
