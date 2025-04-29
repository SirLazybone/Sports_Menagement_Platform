package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;

import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserOrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/org_com")
public class OrgComController {
    private final OrgComService orgComService;
    private final UserService userService;
    private final UserOrgComService userOrgComService;
    private final TournamentService tournamentService;

    private final AccessService accessService;

    public OrgComController(OrgComService orgComService, UserService userService, UserOrgComService userOrgComService,
                            TournamentService tournamentService, AccessService accessService) {
        this.orgComService = orgComService;
        this.userService = userService;
        this.userOrgComService = userOrgComService;
        this.tournamentService = tournamentService;
        this.accessService = accessService;
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
            return "org_com/new_orgcom";
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
    public String createInvitation(@PathVariable UUID orgComId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserChiefOfOrgCom(user.getId(), orgComId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("invitation", new InvitationOrgComDTO());
        model.addAttribute("orgComId", orgComId);
        return "org_com/new_invitation";
    }

    @PostMapping("/send_invitation/{orgComId}")
    public String sendInvitation(@PathVariable UUID orgComId, @Valid @ModelAttribute("invitation") InvitationOrgComDTO invitation, BindingResult bindingResult, Model model) {
        model.addAttribute("orgComId", orgComId);
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
        try {
            if (!accessService.isUserMemberOfOrgCom(user.getId(), id)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        List<UserOrgComDTO> list = orgComService.getAllUsersByOrgComId(id);
        List<Tournament> tournaments = tournamentService.getAllTournamentsOfOrgCom(id);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no members");
            model.addAttribute("members", list);
            return "org_com/view";
        }
        Org orgRole = Org.NONE;
        try {
            orgRole = orgComService.getOrgRoleByUserAndOrgCom(user.getId(), id);
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("members", list);
        model.addAttribute("role", orgRole);
        model.addAttribute("orgComId", id);
        model.addAttribute("orgComName", orgComService.getById(id).getName());
        model.addAttribute("orgRoles", new OrgRoleDTO());
        model.addAttribute("invitationStatuses", new InvitationStatusDTO());
        model.addAttribute("tournaments", tournaments);
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
    public String editOrgCom(@PathVariable UUID orgComId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserChiefOfOrgCom(user.getId(), orgComId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("orgCom", new OrgComDTO());
        model.addAttribute("orgComId", orgComId);
        return "org_com/edit";
    }


    @GetMapping("/tournaments/{orgComId}")
    public String showTournaments(@PathVariable UUID orgComId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserMemberOfOrgCom(user.getId(), orgComId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        List<UserOrgCom> userOrgComs = userOrgComService.getUsersOrgComByOrgComId(orgComId);
        List<Tournament> tournaments = new ArrayList<>();
        for (UserOrgCom userOrgCom : userOrgComs) {
            List<Tournament> userTournaments = tournamentService.getAllTournamentsByUserOrgComId(userOrgCom.getId());
            tournaments.addAll(userTournaments);
        }
        model.addAttribute("tournaments", tournaments);
        return "org_com/tournaments";
    }

    @PostMapping("/edit/{orgComId}")
    public String editOrgCom(@PathVariable UUID orgComId, @Valid @ModelAttribute("orgCom") OrgComDTO orgComDTO, BindingResult bindingResult, Model model) {
        model.addAttribute("orgComId", orgComId);
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
