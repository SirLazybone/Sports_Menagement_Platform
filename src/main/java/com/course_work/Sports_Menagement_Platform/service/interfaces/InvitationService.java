package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.User;

import java.util.List;
import java.util.UUID;

public interface InvitationService {
    List<Invitation> getAllInvitations(User user);
    void sendInvitation(String inviteeTel, String orgComName, User Inviter);
    Invitation getInvitationById(UUID id);
    void deleteInvitation(UUID id);
}
