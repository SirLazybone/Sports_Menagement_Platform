package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByName(String username);
    Optional<User> findByTel(String tel);
    Optional<User> findById(UUID id);
}
