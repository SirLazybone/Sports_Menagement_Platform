package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    List<Invitation> findByInviteeAndStatus(User invitee, InvitationStatus status);
}
