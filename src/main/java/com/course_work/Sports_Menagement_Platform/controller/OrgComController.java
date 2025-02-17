package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.service.impl.OrgComServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.InvitationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/org_com")
public class OrgComController {
    private final OrgComService orgComService;
    private final InvitationService invitationService;
    private final UserService userService;
    public OrgComController(OrgComService orgComService, InvitationService invitationService, UserService userService) {
        this.orgComService = orgComService;
        this.invitationService = invitationService;
        this.userService = userService;
    }

    @GetMapping("/new")
    public String newOrgCom(Model model) {
        model.addAttribute("orgcom", new OrgComDTO());
        return "org_com/new_orgcom";
    }


    @PostMapping("/create")
    public String createOrgCom(@Valid @ModelAttribute("orgcom") OrgComDTO orgComDTO, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "org_com/new_orgcom";
        }
        try {
            orgComService.create(orgComDTO.getName(), user);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }

    @GetMapping("/invitations")
    public String showInvitations(Model model, @AuthenticationPrincipal User user) {
        List<UserOrgCom> list = orgComService.getAllInvitationsPending(user);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no invitations yet");
        }
        model.addAttribute("invitations", list);
        return "org_com/invitations";
    }

    @GetMapping("/create_invitation/{orgComId}")
    public String createInvitation(@PathVariable UUID orgComId, Model model) {
        model.addAttribute("invitation", new InvitationOrgComDTO());
        model.addAttribute("orgComId", orgComId);
        return "org_com/new_invitation";
    }

    @PostMapping("/send_invitation/{orgComId}")
    public String sendInvitation(@PathVariable @ModelAttribute("orgComId") UUID orgComId, @Valid @ModelAttribute("invitation") InvitationOrgComDTO invitation, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "org_com/new_invitation";
        }
        try {
            OrgCom orgCom = orgComService.getById(orgComId);
            orgComService.createInvitation(orgCom, userService.findByTel(invitation.getTel()), invitation.getOrgRole(), invitation.getIsRef());
        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "org_com/new_invitation";
        } catch (Exception e) {
            model.addAttribute("error", "smth really wrong");
            return "org_com/new_invitation";
        }
        return "redirect:/org_com/view/" + orgComId.toString();
    }

    @PostMapping("/invitation/{id}/accept")
    public String acceptInvitation(@PathVariable("id") UUID userOrgComId, Model model) {
        try {
            orgComService.acceptInvitation(userOrgComId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/org_com/invitations";
        }

        return "org_com/invitations";
    }

    @PostMapping("/invitation/{id}/decline")
    public String declineInvitation(@PathVariable("id") UUID userOrgComId, Model model) {
        try {
            orgComService.declineInvitation(userOrgComId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "org_com/invitations";
    }

    @GetMapping("/show_all")
    public String showAllOrgComs(Model model, @AuthenticationPrincipal User user) {
        List<OrgCom> list = orgComService.getAllActiveOrgComByUser(user);

        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no any orgcoms yet");
            return "org_com/show_all";
        }
        model.addAttribute("orgcoms", list);
        return "org_com/show_all";
    }

    @GetMapping("/view/{id}")
    public String viewOrgCom(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        List<UserOrgComDTO> list = orgComService.getAllUsersByOrgComId(id);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no members");
            model.addAttribute("members", list);
            return "org_com/view";
        }
        Org orgRole = orgComService.getOrgRoleByUserAndOrgCom(user.getId(), id);
        model.addAttribute("members", list);
        model.addAttribute("role", orgRole);
        model.addAttribute("orgComId", id);
        model.addAttribute("orgComName", orgComService.getById(id).getName());
        model.addAttribute("orgRoles", new OrgRoleDTO());
        model.addAttribute("invitationStatuses", new InvitationStatusDTO());
        return "org_com/view";
    }

    @PostMapping("/left/{id}")
    public String leftOrgCom(@PathVariable("id") UUID orgComId, Model model, @AuthenticationPrincipal User user) {
        try {
            orgComService.leftOrgCom(orgComId, user); // TODO: Проверить, не явлется ли пользователь единственным шефом
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/org_com/show_all";
    }

    @PostMapping("/kick/{orgComId}/{userTel}")
    public String kickUser(@PathVariable("orgComId") UUID orgComId, @PathVariable("userTel") String tel, Model model) {
        try {
            User user = userService.findByTel(tel);
            orgComService.kickUser(orgComId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/org_com/view/" + orgComId.toString();
    }

    @PostMapping("/cancel/{orgComId}/{userTel}")
    public String cancelInvitation(@PathVariable("orgComId") UUID orgComId, @PathVariable("userTel") String tel, Model model) {
        try {
            User user = userService.findByTel(tel);
            orgComService.cancelInvitation(orgComId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/org_com/view/" + orgComId.toString();
    }

    @GetMapping("/edit/{orgComId}")
    public String editOrgCom(@PathVariable UUID orgComId, Model model) {
        model.addAttribute("orgCom", new OrgComDTO());
        model.addAttribute("orgComId", orgComId);
        return "org_com/edit";
    }

    @PostMapping("/edit/{orgComId}")
    public String editOrgCom(@PathVariable @ModelAttribute("orgComId") UUID orgComId, @Valid @ModelAttribute("orgCom") OrgComDTO orgComDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "org_com/edit";
        }

        try {
            orgComService.editOrgCom(orgComId, orgComDTO);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "org_com/edit";
        }

        return "redirect:/org_com/view/" + orgComId.toString();
    }
}
