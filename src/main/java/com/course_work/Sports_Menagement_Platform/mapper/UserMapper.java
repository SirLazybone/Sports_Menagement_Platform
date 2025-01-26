package com.course_work.Sports_Menagement_Platform.mapper;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.UserCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User DTOToEntity(UserCreationDTO userCreationDTO) {
        User user = new User();
        user.setName(userCreationDTO.getName());
        user.setPassword(userCreationDTO.getPassword());
        user.setTel(userCreationDTO.getTel());
        return user;
    }

    public UserDTO EntityToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setSurname(user.getSurname());
        userDTO.setTel(user.getTel());
        return userDTO;
    }
}
