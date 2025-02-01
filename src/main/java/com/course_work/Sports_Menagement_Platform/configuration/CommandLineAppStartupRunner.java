package com.course_work.Sports_Menagement_Platform.configuration;

import com.course_work.Sports_Menagement_Platform.data.enums.Role;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.repositories.UserRepository;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private final FirstUserParam firstUserParam = new FirstUserParam();
    @Autowired
    UserServiceImpl userService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    private void createFirstUser() {
        User user = null;
        try {
            user = userService.findByUsername(firstUserParam.getName());
        } catch (UsernameNotFoundException e) {
            user = User.builder().name(firstUserParam.getName())
                    .surname(firstUserParam.getSurname())
                    .tel(firstUserParam.getTel())
                    .password(passwordEncoder.encode(firstUserParam.getPassword()))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(user);
        }
    }
    @Override
    public void run(String... args) throws Exception {
        createFirstUser();
    }
}
