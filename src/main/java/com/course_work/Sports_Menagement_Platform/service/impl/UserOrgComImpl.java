package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.repositories.UserOrgComRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserOrgComService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserOrgComImpl implements UserOrgComService {
    private final UserOrgComRepository userOrgComRepository;

    public UserOrgComImpl(UserOrgComRepository userOrgComRepository) {
        this.userOrgComRepository = userOrgComRepository;
    }


    @Override
    public UserOrgCom getUserOrgComByUserIdAndOrgComId(UUID userId, UUID orgComId) {
        return userOrgComRepository.findByUser_IdAndOrgCom_Id(userId, orgComId).orElseThrow(() -> new RuntimeException("UserOrgCom not found"));
    }




    @Override
    @Transactional
    public void setRole(UUID userId, UUID orgComId, Org role) {
        UserOrgCom userOrgCom = getUserOrgComByUserIdAndOrgComId(userId, orgComId);
        userOrgCom.setOrgRole(role);
        userOrgComRepository.save(userOrgCom);
    }


    @Override
    @Transactional
    public void setRefereeStatus(UUID userId, UUID orgComId, boolean status) {
        UserOrgCom userOrgCom = getUserOrgComByUserIdAndOrgComId(userId, orgComId);
        userOrgCom.setRef(status);
        userOrgComRepository.save(userOrgCom);
    }


    @Override
    public List<UserOrgCom> getUsersOrgComByOrgComId(UUID orgComId) {
        return userOrgComRepository.findUsersByOrgComIdNotDTO(orgComId);
    }

    @Override
    public List<UserOrgCom> findAllByUserId(UUID userId) {
        return userOrgComRepository.findAllByUserId(userId);
    }

    @Override
    public int countChiefs(UUID orgComId) {
        return userOrgComRepository.countChiefs(orgComId);
    }
}
