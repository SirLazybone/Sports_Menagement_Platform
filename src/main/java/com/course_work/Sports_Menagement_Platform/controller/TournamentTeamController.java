package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.TeamTournament;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.dto.ApplicationDTO;
import com.course_work.Sports_Menagement_Platform.dto.RatingLineDTO;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/applications")
public class TournamentTeamController {

    private final UserTeamService userTeamService;
    private final TeamService teamService;
    private final TournamentService tournamentService;
    private final AccessService accessService;

    public TournamentTeamController(UserTeamService userTeamService, TeamService teamService, TournamentService tournamentService, AccessService accessService) {
        this.userTeamService = userTeamService;
        this.teamService = teamService;
        this.tournamentService = tournamentService;
        this.accessService = accessService;
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
        boolean actions_allowed = false;
        try {
            actions_allowed = tournamentService.isUserChiefOfTournament(user.getId(), tournamentId);
        } catch (RuntimeException ignored) {}
        List<TeamTournament> list = tournamentService.getCurrAppl(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no applications from teams yet");
        }
        model.addAttribute("applications", list);
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("actions_allowed", actions_allowed);



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


    @GetMapping("/rating/{tournamentId}")
    public String rating(@PathVariable UUID tournamentId, @AuthenticationPrincipal User user, Model model) {
        try {
            tournamentService.getById(tournamentId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Нет такого турнира");
        }
        boolean isOrg = false;
        try {
            isOrg = accessService.isUserOrgOfTournament(user.getId(), tournamentId);
        } catch (RuntimeException ignored) {}
        List<TeamTournament> teamTournamentList = tournamentService.getCurrentParticipants(tournamentId);
        List<RatingLineDTO> ratingLineDTOS = new ArrayList<>();
        Tournament tournament = tournamentService.getById(tournamentId);
        try {
            ratingLineDTOS = tournamentService.getRating(teamTournamentList);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("sport", tournament.getSport());
        model.addAttribute("teams", ratingLineDTOS);
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("isOrg", isOrg);
        return "tournament/rating";
    }

    @PostMapping("/rating/{tournamentId}")
    public String saveRating(@PathVariable UUID tournamentId, @AuthenticationPrincipal User user, @RequestParam Map<String, String> formData) {
        // ford data имеет формат {"teamtourID" : "on"}, только те, которые выбраны

        tournamentService.updatePlayOffTeams(tournamentId, formData.keySet().stream().map(i -> UUID.fromString(i)).collect(Collectors.toList()));
        return "redirect:/applications/rating/" + tournamentId.toString();
    }

    @PostMapping("/cancel/{tournamentId}/{teamId}")
    public String cancelApplication(@PathVariable("tournamentId") UUID tournamentId, @PathVariable("teamId") UUID teamId, @AuthenticationPrincipal User user) {
        TeamTournament teamTournament;
        try {
            teamTournament = tournamentService.getTeamTournament(teamId, tournamentId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        try {
            List<UserTeam> userTeamList = userTeamService.findByUserId(user.getId()).stream().filter(i -> i.getTeam().getId().equals(teamTournament.getTeam().getId())).collect(Collectors.toList());
            if (userTeamList.isEmpty()) {
                throw new AccessDeniedException("Вы не состоите в этой команде");
            }
            accessService.isUserCap(userTeamList.get(0).getId());
        } catch (RuntimeException e) {
            throw new AccessDeniedException("Только капитан может отменять заявку");
        }

        tournamentService.cancelApplication(tournamentId, teamId);
        return "redirect:/team/view/" + teamId.toString();
    }
}
