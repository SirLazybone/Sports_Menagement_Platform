package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;

import java.util.List;

public interface OrgComService {
    OrgCom create(String name, User user);
    List<User> getAllUsers(OrgCom orgCom);
}
