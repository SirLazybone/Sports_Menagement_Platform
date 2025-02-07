package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserOrgComRepository extends JpaRepository<UserOrgCom, UUID> {
}
