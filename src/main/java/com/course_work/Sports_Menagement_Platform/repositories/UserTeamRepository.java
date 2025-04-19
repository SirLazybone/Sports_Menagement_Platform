package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.UserTeamDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, UUID> {
    @Query("SELECT ut.team " +
            "FROM UserTeam ut " +
            "WHERE ut.user.id = :userId " +
            "AND ut.invitationStatus = com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus.ACCEPTED")
    List<Team> findAllActiveTeamByUser(@Param("userId") UUID userId);

    @Query("SELECT ut.user " +
            "FROM UserTeam ut " +
            "WHERE ut.team.id = :teamId")
    List<User> findAllUserByTeam(@Param("teamId") UUID teamId);

    Optional<UserTeam> findByUser_IdAndTeam_Id(UUID userId, UUID teamId);

    @Query("SELECT new com.course_work.Sports_Menagement_Platform.dto.UserTeamDTO(ut.userTeamId, ut.isCap, ut.isPlaying, u.name, u.surname, u.tel, ut.invitationStatus) " +
            "FROM UserTeam ut " +
            "JOIN ut.user u " +
            "WHERE ut.team.id = :teamId")
    List<UserTeamDTO> findAllUserByTeamAndGetDTO(@Param("teamId") UUID teamId);
    List<UserTeam> findByUser(User user);
}
