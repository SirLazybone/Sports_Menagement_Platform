package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {


    @Query("SELECT t FROM Tournament t WHERE t.userOrgCom.id = :userOrgComId")

    List<Tournament> findAllTournamentsByUserOrgComId(@Param("userOrgComId") UUID userOrgComId);
}
