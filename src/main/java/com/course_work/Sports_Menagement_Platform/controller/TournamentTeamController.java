package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.TeamTournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserTeamService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/applications")
public class TournamentTeamController {

    private final UserTeamService userTeamService;
    private final TeamService teamService;
    private final TournamentService tournamentService;

    public TournamentTeamController(UserTeamService userTeamService, TeamService teamService, TournamentService tournamentService) {
        this.userTeamService = userTeamService;
        this.teamService = teamService;
        this.tournamentService = tournamentService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_application/{id}")
    public String createApplication(@PathVariable UUID id, @Valid @ModelAttribute("application") ApplicationDTO applicationDTO, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("tournamentId", id);

        if (bindingResult.hasErrors()) {
            return "applications/create_application";
        }

        try {
            tournamentService.createApplication(applicationDTO, id, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "applications/create_application";
        }

        return "redirect:/home";
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create_application/{id}")
    public String createApplication(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user ) {
        model.addAttribute("tournamentId", id);
        model.addAttribute("application", new ApplicationDTO());
        model.addAttribute("teams", teamService.findTeamsWhereUserIsCap(user));
        return "applications/create_application";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/applications/{tournamentId}")
    public String showApplications(@PathVariable UUID tournamentId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {

        List<TeamTournament> list = tournamentService.getCurrAppl(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no applications from teams yet");
        }
        model.addAttribute("applications", list);
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("actions_allowed", tournamentService.isUserChiefOfTournament(user.getId(), tournamentId));



        return "applications/tournament_applications";
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/approve/{tournamentId}/{teamId}")
    public String approveApplication(@PathVariable UUID tournamentId, @PathVariable UUID teamId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can check applications");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }
        try {
            tournamentService.approveApplication(tournamentId, teamId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tournament/applications/" + tournamentId.toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reject/{tournamentId}/{teamId}")
    public String rejectApplication(@PathVariable UUID tournamentId, @PathVariable UUID teamId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can check applications");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }
        try {
            tournamentService.rejectApplication(tournamentId, teamId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/tournament/applications/" + tournamentId.toString();
    }
}
