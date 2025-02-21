package com.course_work.Sports_Menagement_Platform.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.course_work.Sports_Menagement_Platform.data.models.AfterMatchPenalty;

import java.util.UUID;

@Repository
public interface AfterMatchPenaltyRepository extends JpaRepository<AfterMatchPenalty, UUID> {
}
