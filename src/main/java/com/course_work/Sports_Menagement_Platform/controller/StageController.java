package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("stage")
public class StageController {
    private final StageService stageService;
    private final TournamentService tournamentService;
    private final MatchService matchService;
    public StageController(StageService stageService, TournamentService tournamentService,
                           MatchService matchService) {
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.matchService = matchService;
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create_stage/{tournamentId}")
    public String createStages(@PathVariable UUID tournamentId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create stage");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tournament/view/" + tournamentId.toString();
        }


        model.addAttribute("stage", new StageCreationDTO());
        model.addAttribute("group", new GroupDTO());
        model.addAttribute("tournamentId", tournamentId);
        List<Stage> list = stageService.getStagesByTournament(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "Этапы пока не созданы или не опубликованы");
        }
        model.addAttribute("stages", list);
        return "stage/create_stage";
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
            return "stage/create_stage";
        }

        try {
            stageService.createStage(stageDTO, tournamentId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/stage/create_stage/" + tournamentId.toString();
    }

    @GetMapping("/stages/{tournamentId}") // для всех
    public String showStages(@PathVariable UUID tournamentId, Model model, @AuthenticationPrincipal User user) {
        List<Stage> stages = stageService.getStagesByTournament(tournamentId);
        Map<UUID, List<Match>> matches = matchService.getMatchesByStagesMap(stages);
        boolean isRef = tournamentService.isUserRefOfTournament(user.getId(), tournamentId);
        if (stages == null || stages.isEmpty()) {
            model.addAttribute("error", "Этапы пока не созданы или не опубликованы");
        }
        model.addAttribute("isRef", isRef);
        model.addAttribute("stages", stages);
        model.addAttribute("matchesMap", matches);
        return "stage/stages";
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
        return "redirect:stage/create_stage/" + stageId.toString();
    }
}
