package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    @Query("SELECT g FROM Goal g WHERE g.match.id = :matchId")
    List<Goal> findAllByMatchId(@Param("matchId") UUID matchId);
}
