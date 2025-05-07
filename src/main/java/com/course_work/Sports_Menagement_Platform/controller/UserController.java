package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.dto.ChangePasswordDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.FileStorageService;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final TeamService teamService;
    private final OrgComService orgComService;

    public UserController(UserService userService, FileStorageService fileStorageService,
                          TeamService teamService, OrgComService orgComService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
        this.teamService = teamService;
        this.orgComService = orgComService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserDTO userDTO = userService.getDTOUser(userDetails.getUsername());
        User user = userService.findByTel(userDetails.getUsername());
        model.addAttribute("user", userDTO);
        model.addAttribute("isOwnProfile", true);
        try {
            List<Team> userTeams = teamService.getAllActiveTeamByUser(user);
            model.addAttribute("userTeams", userTeams);
        } catch (RuntimeException ignored) {}
        try {
            List<OrgCom> userOrgComs = orgComService.getAllActiveOrgComByUser(user);
            model.addAttribute("userOrgComs", userOrgComs);
        } catch (RuntimeException ignored) {}
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

    @GetMapping("/invitations")
    public String showAllInvitations(Model model, @AuthenticationPrincipal User user){
        try {
            List<UserTeam> teamInvitations = teamService.getActiveInvitations(user);
            List<UserOrgCom> orgComInvitations = orgComService.getAllInvitationsPending(user);
            if (teamInvitations == null || teamInvitations.isEmpty()) {
                model.addAttribute("teamError", "Нет приглашений в команды");
            }
            if (orgComInvitations == null || orgComInvitations.isEmpty()) {
                model.addAttribute("orgComError", "Нет приглашений в оргкомитеты");
            }
            model.addAttribute("teamInvitations", teamInvitations);
            model.addAttribute("orgComInvitations", orgComInvitations);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "user/invitations";
    }

    @PostMapping("/invitation_team/{id}/accept")
    public String acceptTeamInvitation(@PathVariable("id") UUID userTeamId, RedirectAttributes redirectAttributes) {
        try {
            teamService.acceptInvitation(userTeamId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/invitations";
    }

    @PostMapping("/invitation_team/{id}/decline")
    public String declineTeamInvitation(@PathVariable("id") UUID userTeamId, RedirectAttributes redirectAttributes) {
        try {
            teamService.declineInvitation(userTeamId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/invitations";
    }

    @PostMapping("/invitation_orgcom/{id}/accept")
    public String acceptOrgComInvitation(@PathVariable("id") UUID userOrgComId, RedirectAttributes redirectAttributes) {
        try {
            orgComService.acceptInvitation(userOrgComId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/invitations";
    }

    @PostMapping("/invitation_orgcom/{id}/decline")
    public String declineOrgComInvitation(@PathVariable("id") UUID userOrgComId, RedirectAttributes redirectAttributes) {
        try {
            orgComService.declineInvitation(userOrgComId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user/invitations";
    }

    @GetMapping("/profile/{userId}")
    public String viewUserProfile(@PathVariable UUID userId, Model model) {
        User user;
        try {
            user = userService.findById(userId);
            UserDTO userDTO = userService.getDTOUser(user.getTel());
            model.addAttribute("user", userDTO);
            model.addAttribute("isOwnProfile", false);
        } catch (UsernameNotFoundException e) {
            throw new ResourceNotFoundException("Нет такого пользователя");
        }
        try {
            List<Team> userTeams = teamService.getAllActiveTeamByUser(user);
            model.addAttribute("userTeams", userTeams);
        } catch (RuntimeException ignored) {}
        try {
            List<OrgCom> userOrgComs = orgComService.getAllActiveOrgComByUser(user);
            model.addAttribute("userOrgComs", userOrgComs);
        } catch (RuntimeException ignored) {}
        return "user/profile";
    }
}
