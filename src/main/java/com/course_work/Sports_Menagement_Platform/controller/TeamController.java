package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/team")
public class TeamController {
    private final UserService userService;
    private final TeamService teamService;
    public TeamController(UserService userService, TeamService teamService) {
        this.userService = userService;
        this.teamService = teamService;
    }

    @GetMapping("/new")
    public String newTeam(Model model) {
        model.addAttribute("team", new TeamDTO());
        model.addAttribute("sports", new SportDTO());
        return "team/new_team";
    }

    @PostMapping("/create")
    public String createTeam(@Valid @ModelAttribute("team") TeamDTO team, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "team/new_team";
        }

        try {
            teamService.createTeam(team, user);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "redirect:/home";
    }

    @GetMapping("show_all")
    public String showAll(Model model, @AuthenticationPrincipal User user) {
        List<Team> list;
        try {
            list = teamService.getAllActiveTeamByUser(user);
            model.addAttribute("teams", list);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "team/show_all";
    }

    @GetMapping("/view/{id}")
    public String viewTeam(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        try {
            List<UserTeamDTO> list = teamService.getAllUserByTeamDTO(id);
            model.addAttribute("members", list);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        Team team = teamService.getById(id);
        model.addAttribute("teamId", team.getId());
        model.addAttribute("team", TeamDTO.builder().name(team.getName()).sport(team.getSport()).countMembers(team.getCountMembers()).build());
        model.addAttribute("invitationStatuses", new InvitationStatusDTO());
        if (teamService.isCap(id, user.getId())) {
            model.addAttribute("isCap", "true");
        }

        return "team/view";
    }

    @GetMapping("/create_invitation/{teamId}")
    public String createInvitation(@PathVariable UUID teamId, Model model) {
        model.addAttribute("invitation", new InvitationTeamDTO());
        model.addAttribute("teamId", teamId);
        return "team/new_invitation";
    }

    @PostMapping("/send_invitation/{teamId}")
    public String sendInvitation(@PathVariable @ModelAttribute("teamId") UUID teamId, @Valid @ModelAttribute("invitation") InvitationTeamDTO invitationTeamDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "team/new_invitation";
        }

        try {
            teamService.createInvitation(teamId, invitationTeamDTO.getIsCap(), userService.findByTel(invitationTeamDTO.getTel()));
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "team/new_invitation";
        } catch (Exception e) {
            model.addAttribute("error", "smth really wrong: " + e.getMessage());
            return "team/new_invitation";
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @GetMapping("/invitations")
    public String getInvitations(Model model, @AuthenticationPrincipal User user) {
        try {
            List<UserTeam> list = teamService.getActiveInvitations(user);
            model.addAttribute("invitations", list);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "team/invitations";
    }

    @PostMapping("/invitation/{id}/accept")
    public String acceptInvitation(@PathVariable("id") UUID userTeamId, Model model) {
        try {
            teamService.acceptInvitation(userTeamId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "team/invitations";
    }

    @PostMapping("/invitation/{id}/decline")
    public String declineInvitation(@PathVariable("id") UUID userTeamId, Model model) {
        try {
            teamService.declineInvitation(userTeamId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "team/invitations";
    }

    @PostMapping("/kick/{teamId}/{userTel}")
    public String kickUser(@PathVariable UUID teamId, @PathVariable String userTel, Model model) {
        try {
            User user = userService.findByTel(userTel);
            teamService.kickUser(teamId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @PostMapping("/cancel/{teamId}/{userTel}")
    public String cancelInvitation(@PathVariable UUID teamId, @PathVariable String userTel, Model model) {
        try {
            User user = userService.findByTel(userTel);
            teamService.cancelInvitation(teamId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @PostMapping("/left/{teamId}")
    public String leftTeam(@PathVariable UUID teamId, @AuthenticationPrincipal User user, Model model) {
        try {
            teamService.leftTeam(teamId, user.getId());
        } catch (Exception e) {
            model.addAttribute("error", "smth really wrong: " + e.getMessage());
        }
        return "redirect:/team/show_all";
    }

    @GetMapping("/edit/{teamId}")
    public String editTeam(@PathVariable UUID teamId, Model model) {
        model.addAttribute("team", new TeamDTO());
        model.addAttribute("teamId", teamId);
        return "team/edit";
    }

    @PostMapping("/edit/{teamId}")
    public String editTeam(@PathVariable @ModelAttribute("teamId") UUID teamId, @Valid @ModelAttribute("team") TeamDTO team, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "team/edit";
        }

        try {
            teamService.editTeam(teamId, team);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "team/edit";
        }

        return "redirect:/team/view/" + teamId.toString();
    }

}
