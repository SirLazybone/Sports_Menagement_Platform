package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;

import java.util.List;
import java.util.UUID;

public interface OrgComService {
    OrgCom create(String name, User user);
    List<User> getAllUsers(OrgCom orgCom);
    OrgCom getByName(String name);
    void addUserToOrgCom(Invitation invitation);
    List<OrgCom> getAllActiveOrgComByUser(User user);
    OrgCom getById(UUID id);
    List<UserOrgComDTO> getAllUsersByOrgComId(UUID id);
    Org getOrgRoleByUserAndOrgCom(UUID userId, UUID orgComId);
    List<UserOrgCom> getAllInvitationsPending(User user);
    void createInvitation(OrgCom orgCom, User user, Org orgRole, boolean is_ref);
    void acceptInvitation(UUID userOrgComId);
    void declineInvitation(UUID userOrgComId);
    void leftOrgCom(UUID orgComID, User user);
    void kickUser(UUID orgComId, UUID userId);
    void cancelInvitation(UUID orgComId, UUID userId);
    void editOrgCom(UUID orgComId, OrgComDTO orgComDTO);
    UserOrgCom getUserOrgComChief(String orgComName, UUID id);
}
