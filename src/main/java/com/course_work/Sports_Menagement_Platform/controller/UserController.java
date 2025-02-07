package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            UserDTO userDTO =  userService.getDTOUser(userDetails.getUsername());
            model.addAttribute("user", userDTO);
        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/home";
        }
        return "profile";
    }

    @GetMapping("/edit")
    public String showEditForm(Principal principal, Model model) {
        User user = userService.findByTel(principal.getName());

        UserDTO userDTO = UserDTO.builder().tel(user.getTel()).name(user.getName()).surname(user.getSurname()).build();

        model.addAttribute("user", userDTO);
        return "edit_profile";
    }

    @PostMapping("/edit")
    public String editProfile(@Valid UserDTO userDTO, BindingResult bindingResult, Model model, Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDTO);
            return "edit_profile";
        }
        User user = userService.findByTel(principal.getName());

        try {
            userService.updateUser(user, userDTO);
        } catch (Exception e) {
            model.addAttribute("user", userDTO);
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/user/profile";
    }

}
