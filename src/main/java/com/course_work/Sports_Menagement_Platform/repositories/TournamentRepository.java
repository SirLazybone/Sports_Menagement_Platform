package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.City;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {


    @Query("SELECT t FROM Tournament t WHERE t.userOrgCom.id = :userOrgComId")

    List<Tournament> findAllTournamentsByUserOrgComId(@Param("userOrgComId") UUID userOrgComId);

    @Query("SELECT t FROM Tournament t JOIN t.userOrgCom uoc WHERE uoc.orgCom.id = :orgcomId")
    List<Tournament> findAllByOrgComId(@Param("orgcomId") UUID orgcomId);


    @Query("SELECT t FROM Tournament t WHERE t.name LIKE CONCAT('%', :name, '%') " +
            "AND t.city IN :cities " +
            "AND t.sport IN :sports " +
            "AND t.minMembers >= :teamSizeFrom " +
            "AND t.minMembers <= :teamSizeTo " +
            "AND t.registerDeadline >= :registrationUntil")
    List<Tournament> search(@Param("name") String name,
                            @Param("cities") List<City> cities,
                            @Param("sports") List<String> sports,
                            @Param("teamSizeFrom") int teamSizeFrom,
                            @Param("teamSizeTo") int teamSizeTo,
                            @Param("registrationUntil") LocalDate registrationUntil);

    @Query("SELECT t FROM Tournament t WHERE t.name LIKE CONCAT('%', :name, '%') " +
            "AND t.sport IN :sports " +
            "AND t.minMembers >= :teamSizeFrom " +
            "AND t.minMembers <= :teamSizeTo " +
            "AND t.registerDeadline >= :registrationUntil")
    List<Tournament> searchWithoutCities(@Param("name") String name,
                            @Param("sports") List<String> sports,
                            @Param("teamSizeFrom") int teamSizeFrom,
                            @Param("teamSizeTo") int teamSizeTo,
                            @Param("registrationUntil") LocalDate registrationUntil);

}
