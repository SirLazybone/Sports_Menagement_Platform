package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;

import java.util.List;
import java.util.UUID;

public interface OrgComService {
    OrgCom create(String name, User user);
    List<User> getAllUsers(OrgCom orgCom);
    OrgCom getByName(String name);
    void addUserToOrgCom(Invitation invitation);
    List<OrgCom> getAllByUser(User user);
    OrgCom getById(UUID id);
    List<UserOrgComDTO> getAllUsersByOrgComId(UUID id);
    Org getOrgRoleByUserAndOrgCom(UUID userId, UUID orgComId);
}
