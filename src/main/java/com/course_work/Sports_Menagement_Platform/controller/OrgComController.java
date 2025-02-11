package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.Invitation;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.InvitationDTO;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserOrgComDTO;
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
    public OrgComController(OrgComService orgComService, InvitationService invitationService) {
        this.orgComService = orgComService;
        this.invitationService = invitationService;
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
        List<Invitation> list = invitationService.getAllInvitations(user);
        model.addAttribute("invitations", list);
        return "org_com/invitations";
    }

    @GetMapping("/create_invitation/{orgComId}")
    public String createInvitation(@PathVariable UUID orgComId, Model model) {
        model.addAttribute("invitation", new InvitationDTO());
        model.addAttribute("orgComId", orgComId);
        return "org_com/new_invitation";
    }

    @PostMapping("/send_invitation/{orgComId}")
    public String sendInvitation(@PathVariable @ModelAttribute("orgComId") UUID orgComId, @Valid @ModelAttribute("invitation") InvitationDTO invitation, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "org_com/new_invitation";
        }
        try {
            OrgCom orgCom = orgComService.getById(orgComId);
            invitationService.sendInvitation(invitation.getTel(), orgCom.getName(), user);
        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            return "org_com/new_invitation";
        } catch (Exception e) {
            model.addAttribute("error", "smth really wrong");
            return "org_com/new_invitation";
        }
        return "redirect:/org_com/view/{orgComId}";
    }

    @PostMapping("/invitation/{id}/accept")
    public String acceptInvitation(@PathVariable UUID id, Model model) {
        try {
            Invitation invitation = invitationService.getInvitationById(id);
            orgComService.addUserToOrgCom(invitation);
            invitationService.deleteInvitation(id);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/org_com/invitations";
        }

        return "org_com/invitations";
    }

    @PostMapping("/invitation/{id}/decline")
    public String declineInvitation(@PathVariable UUID id) {
        invitationService.deleteInvitation(id);
        return "org_com/invitations";
    }

    @GetMapping("/show_all")
    public String showAllOrgComs(Model model, @AuthenticationPrincipal User user) {
        List<OrgCom> list = orgComService.getAllByUser(user);

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
        return "org_com/view";
    }
}
