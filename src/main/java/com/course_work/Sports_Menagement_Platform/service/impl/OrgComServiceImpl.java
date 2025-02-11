package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;
import com.course_work.Sports_Menagement_Platform.mapper.OrgComMapper;
import com.course_work.Sports_Menagement_Platform.repositories.OrgComRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserOrgComRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
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

    @Override
    public OrgCom getByName(String name) {
        return orgComRepository.findByName(name);
    }

    @Override
    public void addUserToOrgCom(Invitation invitation) {
        UserOrgCom userOrgCom = new UserOrgCom();
        userOrgCom.setUser(invitation.getInvitee());
        userOrgCom.setOrgCom(invitation.getOrgCom());
        userOrgCom.setOrgRole(invitation.getOrgRole());

        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public List<OrgCom> getAllByUser(User user) {
        return userOrgComRepository.findOrgComsByUserId(user.getId());
    }

    @Override
    public OrgCom getById(UUID id) {
        return orgComRepository.findById(id).orElseThrow(() -> new RuntimeException("Нет такого оргкомитета"));
    }

    @Override
    public List<UserOrgComDTO> getAllUsersByOrgComId(UUID id) {
        return userOrgComRepository.findUsersByOrgComId(id);
    }

    @Override
    public Org getOrgRoleByUserAndOrgCom(UUID userId, UUID orgComId) {
        UserOrgCom userOrgCom = userOrgComRepository.findByUser_IdAndOrgCom_Id(userId, orgComId).orElseThrow(() -> new RuntimeException("No such UserOrgCom"));
        return userOrgCom.getOrgRole();
    }

    public OrgCom getOrgComFromDTO(OrgComDTO orgComDTO) {
        return orgComMapper.DTOToEntity(orgComDTO);
    }



}
