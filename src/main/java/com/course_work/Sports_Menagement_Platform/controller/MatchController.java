package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.MatchListDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Controller
@RequestMapping("/match")
public class MatchController {
    private final MatchService matchService;
    private final StageService stageService;
    private final TournamentService tournamentService;
    private final GoalService goalService;
    private final SlotService slotService;


    public MatchController(MatchService matchService, StageService stageService,
                           TournamentService tournamentService, GoalService goalService,
                           SlotService slotService, TeamService teamService) {
        this.matchService = matchService;
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.goalService = goalService;
        this.slotService = slotService;
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
            List<Match> existingMatches = matchService.getAllMatches(stageId);
            int newMatchesCount = stage.getWorstPlace() / 2 - existingMatches.size();
            MatchListDTO matchList = new MatchListDTO();
            if (newMatchesCount > 0) {
                matchList.setNewMatches(IntStream.range(0, newMatchesCount)
                        .mapToObj(i -> new MatchDTO())
                        .toList());
            }
            List<Team> allTeams = tournamentService.getAllTeamsByTournamentId(stage.getTournament().getId());
            Set<UUID> participatingTeamIds = existingMatches.stream()
                    .flatMap(match -> Stream.of(match.getTeam1().getId(), match.getTeam2().getId()))
                    .collect(Collectors.toSet());
            List<Team> availableTeams = allTeams.stream()
                    .filter(team -> !participatingTeamIds.contains(team.getId()))
                    .toList();

            List<Slot> availableSlots = slotService.getAllNotInUse();

            model.addAttribute("stage", stage);
            model.addAttribute("availableTeams", availableTeams);
            model.addAttribute("existingMatches", existingMatches);
            model.addAttribute("matchList", matchList);
            model.addAttribute("availableSlots", availableSlots);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }


        return "match/fill_stage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_matches/{stageId}")
    public String createMatches(@PathVariable UUID stageId, @ModelAttribute() MatchListDTO matchList, RedirectAttributes redirectAttributes, Model model, @AuthenticationPrincipal User user) {
        List<MatchDTO> newMatches = matchList.getNewMatches();

        if (newMatches == null || newMatches.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Нет заполненных матчей");
            return "redirect:/match/fill_stage/" + stageId.toString();
        }
        System.out.println(newMatches.toString());
        UUID tournamentId = null;
        try {
            tournamentId = stageService.getTournamentByStage(stageId).getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create match");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            matchService.createMatches(newMatches);
            redirectAttributes.addFlashAttribute("success", "Матч успешно создан!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании матча: " + e.getMessage());
        }
        return "redirect:/match/fill_stage/" + stageId.toString();
    }

    @GetMapping("/matches/{stageId}")
    public String showMatches(@PathVariable UUID stageId, Model model) {
        List<Match> list = matchService.getAllMatches(stageId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "There are no matches yet");
        }
        model.addAttribute("matches", list);
        model.addAttribute("stageId", stageId);

        return "match/matches";
    }

    @GetMapping("/edit/{matchId}")
    public String showAddGoalForm(@PathVariable UUID matchId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(matchId);
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only ref of the tournament can edit match results");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
//            match = matchService.getById(matchId);
            List<Team> teams = List.of(match.getTeam1(), match.getTeam2());
            Map<UUID, List<User>> players = matchService.getTeamMembersMap(match.getTeam1(), match.getTeam2());

            model.addAttribute("match", match);
            model.addAttribute("teams", teams);
            model.addAttribute("playersMap", players);
            model.addAttribute("sport", match.getStage().getTournament().getSport());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("goalDTO", new GoalDTO());

        return "match/edit_match_result";
    }

    @PostMapping("/save_goal")
    public String saveGoal(@ModelAttribute GoalDTO goal, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(goal.getMatchId());
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only ref of the tournament can edit match results");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        goalService.addGoal(goal);
        return "redirect:/match/edit/" + goal.getMatchId().toString();
    }

    @PostMapping("/update_slot/{matchId}")
    public String updateSlot(@PathVariable UUID matchId, @RequestParam UUID slotId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(matchId);
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Only the chief of the tournament can update the slot for a match.");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            matchService.assignSlotToMatch(slotId, matchId);
            redirectAttributes.addFlashAttribute("success", "Slot successfully assigned to the match.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while assigning slot: " + e.getMessage());
        }

        return "redirect:/match/fill_stage/" + match.getStage().getId().toString();
    }
}
