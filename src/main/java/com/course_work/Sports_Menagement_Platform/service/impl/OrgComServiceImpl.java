package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.mapper.OrgComMapper;
import com.course_work.Sports_Menagement_Platform.repositories.OrgComRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserOrgComRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrgComServiceImpl implements OrgComService {
    private final OrgComRepository orgComRepository;
    private final UserOrgComRepository userOrgComRepository;
    private final OrgComMapper orgComMapper;
    public OrgComServiceImpl(OrgComRepository orgComRepository, UserOrgComRepository userOrgComRepository, OrgComMapper orgComMapper) {
        this.orgComRepository = orgComRepository;
        this.userOrgComRepository = userOrgComRepository;
        this.orgComMapper = orgComMapper;
    }

    @Override
    public OrgCom create(String name, User user) {
        if (orgComRepository.existsByName(name)) {
            throw new RuntimeException("Организация с именем" + name + " уже существует");
        }
        OrgCom orgCom = OrgCom.builder().name(name).build();
        orgComRepository.save(orgCom);

        UserOrgCom userOrgCom = UserOrgCom.builder().user(user).orgRole(Org.CHIEF).orgCom(orgCom).build();
        userOrgComRepository.save(userOrgCom);
        return orgCom;
    }

    @Override
    public List<User> getAllUsers(OrgCom orgCom) {
        return orgCom.getUserOrgComList().stream()
                .map(UserOrgCom::getUser)
                .collect(Collectors.toList());
    }

    public OrgCom getOrgComFromDTO(OrgComDTO orgComDTO) {
        return orgComMapper.DTOToEntity(orgComDTO);
    }



}
