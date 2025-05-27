package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.Role;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.ChangePasswordDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import com.course_work.Sports_Menagement_Platform.mapper.UserMapper;
import com.course_work.Sports_Menagement_Platform.repositories.UserRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }
    @Override
    public UserDetails loadUserByUsername(String tel) throws UsernameNotFoundException {
        return userRepository.findByTel(tel).orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким телефоном уже существует"));
    }

    @Override
    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByName(username).orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
    }

    @Override
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found " + id.toString()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByIdWithRelations(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("User not found " + id.toString()));
        
        user.getUserOrgComList().size(); 
        user.getUserTeamList().size();
        

        user.getUserOrgComList().forEach(userOrgCom -> {
            userOrgCom.getOrgCom().getName(); 
        });
        
        user.getUserTeamList().forEach(userTeam -> {
            userTeam.getTeam().getName();
            userTeam.getTeam().getTeamTournamentList().size();
            userTeam.getTeam().getTeamTournamentList().forEach(teamTournament -> {
                teamTournament.getTournament().getName();
                teamTournament.getTournament().getUserOrgCom().getOrgCom().getName();
                teamTournament.getTournament().getCity().getName();
            });
        });
        
        return user;
    }

    @Override
    public User findByTel(String tel) {
        return userRepository.findByTel(tel).orElseThrow(() -> new UsernameNotFoundException("User with tel: " + tel + " not found"));
    }

    @Override
    public User saveNewUser(UserCreationDTO newUser) {
        // Нормализуем номер телефона перед проверкой и сохранением
        String normalizedTel = normalizePhoneNumber(newUser.getTel());
        
        if (userRepository.findByTel(normalizedTel).isPresent()) {
            throw new RuntimeException("Пользователь с таким телефоном уже существует");
        }

        User user = userMapper.DTOToEntity(newUser);
        user.setTel(normalizedTel); // Сохраняем нормализованный номер
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }
    
    /**
     * Нормализует номер телефона, удаляя все символы кроме цифр и приводя к формату +7XXXXXXXXXX
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        
        // Удаляем все символы кроме цифр
        String digitsOnly = phoneNumber.replaceAll("\\D", "");
        
        // Если номер начинается с 8, заменяем на 7
        if (digitsOnly.startsWith("8") && digitsOnly.length() == 11) {
            digitsOnly = "7" + digitsOnly.substring(1);
        }
        
        // Если номер начинается с 7 и имеет 11 цифр, добавляем +
        if (digitsOnly.startsWith("7") && digitsOnly.length() == 11) {
            return "+" + digitsOnly;
        }
        
        // Если номер имеет 10 цифр, добавляем +7
        if (digitsOnly.length() == 10) {
            return "+7" + digitsOnly;
        }
        
        // Возвращаем исходный номер если не удалось нормализовать
        return phoneNumber;
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDTO getDTOUser(String tel) {
        return userMapper.EntityToDTO(findByTel(tel));
    }

    @Override
    @Transactional
    public void updateUser(User user, UserDTO userDTO) {
        if (!userDTO.getName().isEmpty()) {
            user.setName(userDTO.getName());
        }
        if (!userDTO.getSurname().isEmpty()) {
            user.setSurname(userDTO.getSurname());
        }
        if (!userDTO.getTel().isEmpty()) {
            String normalizedTel = normalizePhoneNumber(userDTO.getTel());
            user.setTel(normalizedTel);
        }
        if (userDTO.getPhoto() != null) {
            user.setPhoto(userDTO.getPhoto());
        }

        userRepository.save(user);
    }

    @Override
    public boolean changePassword(UUID userId, ChangePasswordDTO changePasswordDTO) {
        User user = getById(userId);
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Неверный старый пароль!");
        }
        if (!changePasswordDTO.getConfirmPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new RuntimeException("Новые пароли не совпадают!");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
        return true;
    }

}
