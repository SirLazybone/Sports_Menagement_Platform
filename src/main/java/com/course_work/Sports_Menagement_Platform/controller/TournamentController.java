package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.CityService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tournament")
public class TournamentController {
    private final TournamentService tournamentService;
    private final StageService stageService;

    private final CityService cityService;
    public TournamentController(TournamentService tournamentService, StageService stageService, CityService cityService) {
        this.tournamentService = tournamentService;
        this.stageService = stageService;
        this.cityService = cityService;
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

    @GetMapping("/create/{orgComId}")
    public String createTournament(Model model, @PathVariable UUID orgComId) {
        model.addAttribute("tournament", new TournamentDTO());
        model.addAttribute("cities", cityService.getCities());
        model.addAttribute("orgComId", orgComId);
        return "tournament/create";
    }

    @PostMapping("/create/{orgComId}")
    public String createTournament(@Valid @ModelAttribute("tournament") TournamentDTO tournamentDTO, @PathVariable UUID orgComId, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "tournament/create";
        }

        try {
            Tournament tournament = tournamentService.createTournament(tournamentDTO, user, orgComId);
            return "redirect:/tournament/org_com/view/" + tournament.getId();

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        }

    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model) {
        try {
            Tournament tournament = tournamentService.getById(id);
            model.addAttribute("tournament", tournament);
            model.addAttribute("tournamentId", id);
            model.addAttribute("registrationOpen", tournament.getRegisterDeadline().isAfter(ChronoLocalDate.from(LocalDateTime.now())));
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

