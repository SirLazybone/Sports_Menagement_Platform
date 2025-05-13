package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface UserOrgComService {
    UserOrgCom getUserOrgComByUserIdAndOrgComId(UUID userId, UUID orgComId);


    @Transactional
    void setRole(UUID userId, UUID orgComId, Org role);

    @Transactional
    void setRefereeStatus(UUID userId, UUID orgComId, boolean status);

    List<UserOrgCom> getUsersOrgComByOrgComId(UUID orgComId);

    List<UserOrgCom> findAllByUserId(UUID userId);

    int countChiefs(UUID orgComId);
}
