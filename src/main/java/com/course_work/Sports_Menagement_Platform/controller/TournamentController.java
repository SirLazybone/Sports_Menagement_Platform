package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("tournament")
public class TournamentController {
    private final TournamentService tournamentService;
    private final StageService stageService;

    public TournamentController(TournamentService tournamentService, StageService stageService) {
        this.tournamentService = tournamentService;
        this.stageService = stageService;
    }

    @GetMapping("/show_all")
    public String showAll(Model model) {
        List<Tournament> list = tournamentService.getAllTournaments();
        model.addAttribute("tournaments", list);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no tournaments yet");
        }
        return "tournament/show_all";
    }

    @GetMapping("/create")
    public String createTournament(Model model) {
        model.addAttribute("tournament", new TournamentDTO());
        return "tournament/create";
    }

    @PostMapping("/create")
    public String createTournament(@Valid @ModelAttribute("tournament") TournamentDTO tournamentDTO, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "tournament/create";
        }

        try {
            tournamentService.createTournament(tournamentDTO, user);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        }

        return "redirect:/tournament/show_all";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model) {
        try {
            TournamentDTO tournamentDTO = tournamentService.getDTOById(id);
            model.addAttribute("tournament", tournamentDTO);
            model.addAttribute("tournamentId", id);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/show_all";
        }

        return "tournament/view";
    }

//    @GetMapping("/view/{orgcomId}/{tournamentId}")
//    public String viewInOrgCom(@PathVariable UUID orgcomId, @PathVariable UUID tournamentId, @AuthenticationPrincipal User user) {
//
//    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create_application/{id}")
    public String createApplication(@PathVariable UUID id, Model model) {
        model.addAttribute("tournamentId", id);
        model.addAttribute("application", new ApplicationDTO());
        return "tournament/create_application";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_application/{id}")
    public String createApplication(@PathVariable UUID id, @Valid @ModelAttribute("application") ApplicationDTO applicationDTO, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("tournamentId", id);
        if (bindingResult.hasErrors()) {
            return "tournament/create_application";
        }

        try {
            tournamentService.createApplication(applicationDTO, id, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create_application";
        }

        return "redirect:/tournament/show_all";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/applications/{id}")
    public String showApplications(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        if (!tournamentService.isUserChiefOfTournament(user.getId(), id)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can check applications");
            return "redirect:/tournament/view/" + id.toString();
        }
        List<ApplicationDTO> list = tournamentService.getCurrAppl(id);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no applications from teams yet");
        }
        model.addAttribute("applications", list);
        model.addAttribute("tournamentId", id);
        return "tournament/applications";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/applications/approve/{tournamentId}/{teamId}")
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
    @PostMapping("/applications/reject/{tournamentId}/{teamId}")
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

    @GetMapping("/participants/{tournamentId}")
    public String showParticipantsOfTournament(@PathVariable UUID tournamentId, Model model) {
        List<ApplicationDTO> list = tournamentService.getCurrParticipants(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no teams yet");
        }
        model.addAttribute("participants", list);

        return "tournament/participants";
    }

    @GetMapping("/teams/{teamId}")
    public String showTournamentForTeam(@PathVariable UUID teamId, Model model) {
        List<TeamTournamentDTO> list = tournamentService.getTournamentsByTeam(teamId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "Team does not participate in any tournament");
        }
        model.addAttribute("tournaments", list);
        model.addAttribute("applicationStatuses", new ApplicationStatusDTO());

        return "tournament/teams_tournament";
    }

    @GetMapping("org_com/view/{tournamentId}")
    public String viewTournamentFotOrgCom(@PathVariable UUID tournamentId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Tournament tournament = tournamentService.getById(tournamentId);
        if (!tournamentService.isUserMemberOfOrgCom(user.getId(), tournament.getUserOrgCom().getOrgCom())) {
            redirectAttributes.addFlashAttribute("error", "User is not the member of the org com");
            return "redirect:/home";
        }

        try {
            List<Stage> stage = stageService.getStagesByTournament(tournamentId);
            List<Team> teams = tournamentService.getAllTeamsByTournamentId(tournamentId);
            model.addAttribute("stages", stage);
            model.addAttribute("countTeams", teams.size());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "org_com/tournament_view";
    }


//    @PostMapping("/create_group/{tournamentId}")
//    public String createGroup(@PathVariable UUID tournamentId, @Valid @ModelAttribute GroupDTO group, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
//        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
//            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create group");
//            return "redirect:/tournament/view/" + tournamentId.toString();
//        }
//
//        model.addAttribute("tournamentId", tournamentId);
//        if (bindingResult.hasErrors()) {
//            return "tournament/create_stage";
//        }
//
//        try {
//
//        }
//        return null;
//    }
}

