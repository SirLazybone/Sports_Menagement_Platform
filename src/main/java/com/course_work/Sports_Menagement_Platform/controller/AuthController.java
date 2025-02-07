package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.dto.UserCreationDTO;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    private final UserServiceImpl userService;
    public AuthController(UserServiceImpl userService) {
        this.userService = userService;
    }
    @GetMapping("/auth/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserCreationDTO());
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@Valid @ModelAttribute("user") UserCreationDTO user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        try {
            userService.saveNewUser(user);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
//            model.addAttribute("user", new UserCreationDTO());
            return "registration";
        }
        return "redirect:/auth/login";
    }
}
