package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.ChangePasswordDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import com.course_work.Sports_Menagement_Platform.service.FileStorageService;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
        return "user/profile";
    }

    @GetMapping("/edit")
    public String showEditForm(Principal principal, Model model) {
        User user = userService.findByTel(principal.getName());

        UserDTO userDTO = UserDTO.builder()
                .tel(user.getTel())
                .name(user.getName())
                .surname(user.getSurname())
                .photo(user.getPhoto())
                .build();

        model.addAttribute("user", userDTO);
        return "user/edit_profile";
    }

    @PostMapping("/edit")
    public String editProfile(@Valid UserDTO userDTO, 
                            BindingResult bindingResult, 
                            Model model, 
                            Principal principal,
                            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userDTO);
            return "user/edit_profile";
        }
        User user = userService.findByTel(principal.getName());

        try {
            if (photoFile != null && !photoFile.isEmpty()) {
                // Delete old photo if exists
                if (user.getPhoto() != null) {
                    fileStorageService.deleteFile(user.getPhoto());
                }
                // Store new photo
                String fileName = fileStorageService.storeFile(photoFile);
                userDTO.setPhoto(fileName);
            }
            userService.updateUser(user, userDTO);
        } catch (Exception e) {
            model.addAttribute("user", userDTO);
            model.addAttribute("error", e.getMessage());
            return "user/edit_profile";
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/change_password")
    public String changePassword(Model model) {
        model.addAttribute("changePassword", new ChangePasswordDTO());
        return "user/change_password";
    }

    @PostMapping("/change_password")
    public String changePassword(@AuthenticationPrincipal User user, Model model, @ModelAttribute("changePassword") ChangePasswordDTO changePasswordDTO) {
        try {
            userService.changePassword(user.getId(), changePasswordDTO);
            model.addAttribute("success", "Пароль успешно сменён");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "user/change_password";
    }
}
