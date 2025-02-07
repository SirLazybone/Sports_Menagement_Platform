package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.repositories.InvitationRepository;
import com.course_work.Sports_Menagement_Platform.repositories.OrgComRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.InvitationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class InvitationServiceImpl implements InvitationService {
    private final InvitationRepository invitationRepository;
    public final OrgComService orgComService;
    public final UserService userService;
    public InvitationServiceImpl(InvitationRepository invitationRepository, OrgComService orgComService, UserService userService) {
        this.invitationRepository = invitationRepository;
        this.orgComService = orgComService;
        this.userService = userService;
    }

    @Override
    public List<Invitation> getAllInvitations(User user) {
        return invitationRepository.findByInviteeAndStatus(user, InvitationStatus.PENDING);
    }

    @Override
    public void sendInvitation(String inviteeTel, String orgComName, User inviter) {
        User invitee = userService.findByTel(inviteeTel);
        OrgCom orgCom = orgComService.getByName(orgComName);
        Invitation invitation = new Invitation();

        invitation.setInviter(inviter);
        invitation.setInvitee(invitee);
        invitation.setOrgCom(orgCom);

        invitationRepository.save(invitation);
    }

    @Override
    public Invitation getInvitationById(UUID id) {
        return invitationRepository.findById(id).orElseThrow(() -> new RuntimeException("No such invitation presented: " + id));
    }

    @Override
    public void deleteInvitation(UUID id) {
        invitationRepository.deleteById(id);
    }
}
