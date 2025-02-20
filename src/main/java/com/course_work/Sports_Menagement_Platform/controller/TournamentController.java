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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create_stage/{tournamentId}")
    public String createStages(@PathVariable UUID tournamentId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create stage");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        model.addAttribute("stage", new StageCreationDTO());
        model.addAttribute("tournamnetId", tournamentId);
        List<Stage> list = stageService.getStagesByTournament(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "Этапы пока не созданы или не опубликованы");
        }
        model.addAttribute("stages", list);
        return "tournament/create_stage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_stage/{tournamentId}")
    public String createStage(@PathVariable UUID tournamentId, @Valid @ModelAttribute("stage") StageCreationDTO stageDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create stage");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        model.addAttribute("tournamentId", tournamentId);
        if (bindingResult.hasErrors()) {
            return "tournament/create_stage";
        }

        try {
            stageService.createStage(stageDTO, tournamentId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/tournament/create_stage/" + tournamentId.toString();
    }

    @GetMapping("/stages/{tournamentId}") // для всех
    public String showStages(@PathVariable UUID tournamentId, Model model) {
        List<Stage> list = stageService.getStagesByTournament(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "Этапы пока не созданы или не опубликованы");
        }
        model.addAttribute("stages", list);
        return "tournament/stages";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/fill_stage/{stageId}")
    public String fillStage(@PathVariable UUID stageId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        UUID tournamentId = null;
        try {
            tournamentId = stageService.getTournamentByStage(stageId).getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/home";
        }
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can fill stage");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            Stage stage = stageService.getStageById(stageId);
            List<Team> teams = stageService.getTeamsByStageId(stageId);
            model.addAttribute("math", new MatchDTO());
            model.addAttribute("teams", teams);
            model.addAttribute("stage", stage);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "tournament/fill_stage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_matches")
    public String createMatches(@ModelAttribute("match") MatchDTO matchDTO, RedirectAttributes redirectAttributes, Model model, @AuthenticationPrincipal User user) {
        UUID tournamentId = null;
        try {
            tournamentId = stageService.getTournamentByStage(matchDTO.getStageId()).getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/home";
        }
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create match");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            stageService.createMatch(matchDTO);
            redirectAttributes.addFlashAttribute("success", "Матч успешно создан!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании матча: " + e.getMessage());
        }
        return "redirect:/tournament/fill_stage/" + matchDTO.getStageId();
    }

    @PostMapping("/publish_stage/{stageId}")
    public String publishStage(@PathVariable UUID stageId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        UUID tournamentId = null;
        try {
            tournamentId = stageService.getTournamentByStage(stageId).getId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/home";
        }
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create match");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            stageService.publishStage(stageId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:tournament/create_stage/" + stageId.toString();
    }

    @GetMapping("/matches/{stageId}")
    public String showMatches(@PathVariable UUID stageId, Model model) {
        List<Match> list = stageService.getAllMatches(stageId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no matches yet");
        }
        model.addAttribute("matches", list);
        model.addAttribute("stageId", stageId);

        return "tournament/matches";
    }


}

