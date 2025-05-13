package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOrgComRepository extends JpaRepository<UserOrgCom, UUID> {
    @Query("SELECT uoc.orgCom " +
            "FROM UserOrgCom uoc " +
            "WHERE uoc.user.id = :userId " +
            "AND uoc.invitationStatus = com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus.ACCEPTED")
    List<OrgCom> findActiveOrgComsByUserId(@Param("userId") UUID userId);
    List<User> findAllByOrgCom(OrgCom orgCom);
    @Query("SELECT new com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO(u.id, u.name, u.surname, u.tel, uoc.orgRole, uoc.invitationStatus, uoc.isRef) " +
            "FROM UserOrgCom uoc " +
            "JOIN uoc.user u " +
            "WHERE uoc.orgCom.id = :orgComId")
    List<UserOrgComDTO> findUsersByOrgComId(@Param("orgComId") UUID orgComId);

    @Query("SELECT uoc " +
            "FROM UserOrgCom uoc " +
            "JOIN uoc.user u " +
            "WHERE uoc.orgCom.id = :orgComId")
    List<UserOrgCom> findUsersByOrgComIdNotDTO(@Param("orgComId") UUID orgComId);

    Optional<UserOrgCom> findByUser_IdAndOrgCom_Id(UUID userId, UUID orgComId);
    List<UserOrgCom> findByUser(User user);

    @Query("SELECT uoc FROM UserOrgCom uoc JOIN FETCH uoc.orgCom WHERE uoc.user = :user AND uoc.invitationStatus = com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus.PENDING")
    List<UserOrgCom> findPendingInvitationsWithOrgCom(@Param("user") User user);

    @Query("SELECT uoc FROM UserOrgCom uoc WHERE uoc.user.id = :userId AND uoc.invitationStatus = com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus.ACCEPTED")
    List<UserOrgCom> findAllByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(uoc) FROM UserOrgCom uoc WHERE uoc.orgCom.id = :orgComId AND uoc.invitationStatus = com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus.ACCEPTED")
    int countChiefs(@Param("orgComId") UUID orgComId);
}
