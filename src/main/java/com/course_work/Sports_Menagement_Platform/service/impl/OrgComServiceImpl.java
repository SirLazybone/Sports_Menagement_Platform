package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;
import com.course_work.Sports_Menagement_Platform.mapper.OrgComMapper;
import com.course_work.Sports_Menagement_Platform.repositories.OrgComRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserOrgComRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        UserOrgCom userOrgCom = UserOrgCom.builder().user(user).orgRole(Org.CHIEF).orgCom(orgCom).invitationStatus(InvitationStatus.ACCEPTED).build();

//        orgCom.getUserOrgComList().add(userOrgCom);
//        user.getUserOrgComList().add(userOrgCom);

        orgComRepository.save(orgCom);
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
        return orgComRepository.findByName(name).orElseThrow(() -> new RuntimeException("No such OrgCom"));
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
    @Transactional
    public List<OrgCom> getAllActiveOrgComByUser(User user) {
        return userOrgComRepository.findActiveOrgComsByUserId(user.getId());
    }

    @Override
    public OrgCom getById(UUID id) {
        return orgComRepository.findById(id).orElseThrow(() -> new RuntimeException("Нет такого оргкомитета"));
    }

    @Override
    public List<UserOrgComDTO> getAllUsersByOrgComId(UUID id) {
        List<UserOrgComDTO> list = userOrgComRepository.findUsersByOrgComId(id);
        Map<InvitationStatus, Integer> customOrder = Map.of(
                InvitationStatus.ACCEPTED, 1,
                InvitationStatus.PENDING, 2,
                InvitationStatus.DECLINED, 3,
                InvitationStatus.LEFT, 4,
                InvitationStatus.CANCELED, 5,
                InvitationStatus.KICKED, 6
        );
        list.sort(Comparator.comparing(user -> customOrder.get(user.getInvitationStatus())));
        return list;
    }

    @Override
    public UserOrgCom getUserOrgComByUserAndOrgCom(UUID userId, UUID orgComId) {
        return userOrgComRepository.findByUser_IdAndOrgCom_Id(userId, orgComId).orElseThrow(() -> new RuntimeException("Пользователь не относится к данной организации"));
    }

    @Override
    public Org getOrgRoleByUserAndOrgCom(UUID userId, UUID orgComId) {
        UserOrgCom userOrgCom = getUserOrgComByUserAndOrgCom(userId, orgComId);
        return userOrgCom.getOrgRole();
    }

    @Override
    public List<UserOrgCom> getAllInvitationsPending(User user) {
        List<UserOrgCom> list = userOrgComRepository.findByUser(user);
        return list.stream().filter(x -> x.getInvitationStatus() == InvitationStatus.PENDING).toList();
    }

    @Override
    public void createInvitation(OrgCom orgCom, User user, Org orgRole, boolean isRef) {
        Optional<UserOrgCom> optionalUserOrgCom = userOrgComRepository.findByUser_IdAndOrgCom_Id(user.getId(), orgCom.getId());
        if (optionalUserOrgCom.isPresent()) {
            UserOrgCom presentedUserOrgCom = optionalUserOrgCom.get();
            presentedUserOrgCom.setInvitationStatus(InvitationStatus.PENDING);
            presentedUserOrgCom.setOrgRole(orgRole);
            presentedUserOrgCom.setRef(isRef);
            userOrgComRepository.save(presentedUserOrgCom);
            return;
        }
        UserOrgCom userOrgCom = UserOrgCom.builder().orgRole(orgRole).user(user).orgCom(orgCom).invitationStatus(InvitationStatus.PENDING).isRef(isRef).build();
        orgCom.getUserOrgComList().add(userOrgCom);
        user.getUserOrgComList().add(userOrgCom);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void acceptInvitation(UUID userOrgComId) {
        UserOrgCom userOrgCom = userOrgComRepository.findById(userOrgComId).orElseThrow(() -> new RuntimeException("No such UserOrgCom"));
        userOrgCom.setInvitationStatus(InvitationStatus.ACCEPTED);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void declineInvitation(UUID userOrgComId) {
        UserOrgCom userOrgCom = userOrgComRepository.findById(userOrgComId).orElseThrow(() -> new RuntimeException("No such UserOrgCom"));
        userOrgCom.setInvitationStatus(InvitationStatus.DECLINED);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void leftOrgCom(UUID orgComId, User user) {
        UserOrgCom userOrgCom = getUserOrgComByUserAndOrgCom(user.getId(), orgComId);
        userOrgCom.setInvitationStatus(InvitationStatus.LEFT);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void kickUser(UUID orgComId, UUID userId) {
        UserOrgCom userOrgCom = getUserOrgComByUserAndOrgCom(userId, orgComId);
        userOrgCom.setInvitationStatus(InvitationStatus.KICKED);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void cancelInvitation(UUID orgComId, UUID userId) {
        UserOrgCom userOrgCom = getUserOrgComByUserAndOrgCom(userId, orgComId);
        userOrgCom.setInvitationStatus(InvitationStatus.CANCELED);
        userOrgComRepository.save(userOrgCom);
    }

    @Override
    public void editOrgCom(UUID orgComId, OrgComDTO orgComDTO) {
        Optional<OrgCom> orgCom = orgComRepository.findByName(orgComDTO.getName());
        if (orgCom.isPresent()) {
            throw new RuntimeException("Организация с таким именем уже существует");
        }
        OrgCom edited = orgComRepository.findById(orgComId).orElseThrow(() -> new RuntimeException("No such OrgCom"));
        edited.setName(orgComDTO.getName());
        orgComRepository.save(edited);
    }

    @Override
    public UserOrgCom getUserOrgComChief(String orgComName, UUID userId) {
        OrgCom orgCom = orgComRepository.findByName(orgComName).orElseThrow(() -> new RuntimeException("Такой организации не сущетсвует"));
        UserOrgCom userOrgCom = getUserOrgComByUserAndOrgCom(userId, orgCom.getId());
        if (userOrgCom.getInvitationStatus() != InvitationStatus.ACCEPTED) {
            throw new RuntimeException("Не активный пользователь не может создать турнир от имени организации");
        }
        if (userOrgCom.getOrgRole() != Org.CHIEF) {
            throw new RuntimeException("Не Старший организатор не может создать турнир");
        }
        return userOrgCom;
    }

    @Override
    public boolean isUserOfOrgComRef(UUID userId, UUID orgComId) {
        return getUserOrgComByUserAndOrgCom(userId, orgComId).isRef();
    }

    @Override
    public boolean isUserOfOrgComChief(UUID userId, UUID orgComId) {
        return getUserOrgComByUserAndOrgCom(userId, orgComId).getOrgRole().equals(Org.CHIEF);
    }


}
