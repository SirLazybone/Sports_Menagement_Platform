package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.TeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserTeamDTO;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    Team createTeam(TeamDTO teamDTO, User user);
    Team getById(UUID id);
    List<Team> getAllActiveTeamByUser(User user);
    List<User> getAllUserByTeam(UUID teamId);
    Boolean isCap(UUID teamId, UUID userId);
    List<UserTeamDTO> getAllUserByTeamDTO(UUID teamId);
    void createInvitation(UUID teamId, Boolean isCap, Boolean notPlaying, User user);
    List<UserTeam> getActiveInvitations(User user);
    void acceptInvitation(UUID userTeamId);
    void declineInvitation(UUID userTeamId);
    void kickUser(UUID teamId, UUID userId);
    void cancelInvitation(UUID teamId, UUID userId);
    void leftTeam(UUID teamId, UUID userId);
    void editTeam(UUID teamId, TeamDTO teamDTO);

    Team getByName(String name);

    List<Team> findTeamsWhereUserIsCap(User user);

    boolean isCapOfUserTeam(UUID userTeamId);

    UserTeam getUserTeamByUserAndTeam(UUID userId, UUID teamId);
}
