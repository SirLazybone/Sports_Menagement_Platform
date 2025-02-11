package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
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
    @Query("SELECT new com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO(u.name, u.surname, u.tel, uoc.orgRole, uoc.invitationStatus) " +
            "FROM UserOrgCom uoc " +
            "JOIN uoc.user u " +
            "WHERE uoc.orgCom.id = :orgComId")
    List<UserOrgComDTO> findUsersByOrgComId(@Param("orgComId") UUID orgComId);

    Optional<UserOrgCom> findByUser_IdAndOrgCom_Id(UUID userId, UUID orgComId);
    List<UserOrgCom> findByUser(User user);
}
