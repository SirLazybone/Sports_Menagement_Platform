package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.UserCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User getById(UUID userId);
    User findByUsername(String username);
    User saveNewUser(UserCreationDTO user);
    void deleteUser(UUID id);
    List<User> findAll();
    UserDTO getDTOUser(String tel);
    User findByTel(String tel);
    void updateUser(User user, UserDTO userDTO);


}
