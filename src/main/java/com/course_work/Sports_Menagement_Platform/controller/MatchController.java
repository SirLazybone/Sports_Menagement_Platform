package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.data.util.Pair;
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

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/match")
public class MatchController {
    private final MatchService matchService;
    private final StageService stageService;
    private final TournamentService tournamentService;
    private final GoalService goalService;
    private final SlotService slotService;
    private final TeamService teamService;
    private final UserService userService;
    private final AfterMatchPenaltyService afterMatchPenaltyService;

    public MatchController(MatchService matchService, StageService stageService,
                           TournamentService tournamentService, GoalService goalService,
                           SlotService slotService, TeamService teamService, UserService userService,
                           AfterMatchPenaltyService afterMatchPenaltyService) {
        this.matchService = matchService;
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.goalService = goalService;
        this.slotService = slotService;
        this.teamService = teamService;
        this.userService = userService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
    }

    @GetMapping("/fill_group_stage/{stageId}")
    public String fillGroupStage(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        Stage stage = stageService.getStageById(stageId);
        if (stage.getBestPlace() != 0) {
            model.addAttribute("error", "Выбран не групповой этап");
            return "redirect:/home";
        }
        Map<Group, List<Match>> matches = matchService.createGroupMatchIfNotCreated(stageId);
        StageStatus stageStatus = stageService.getStageStatus(stage);
        if (stageStatus != StageStatus.TEAMS_KNOWN) {
            model.addAttribute("error", "Настройка расписания этапа недоступна");
            return "redirect:/home";
        }
        List<Slot> availableSlots = slotService.getAllSlotsForStage(stage);
        model.addAttribute("matches", matches);
        model.addAttribute("slot", availableSlots);
        model.addAttribute("stageId", stageId);

        return "match/fill_group";
    }

    @PostMapping("/fill_group_stage/{stageId}")
    public String fillGroupStagePost(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes, @RequestParam Map<String, String> slotAssignments) {
        Map<UUID, UUID> assignments = slotAssignments.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        entry -> UUID.fromString(entry.getKey().substring(
                                entry.getKey().indexOf('[') + 1,
                                entry.getKey().indexOf(']'))),
                        entry -> UUID.fromString(entry.getValue())
                ));  // {match : slot}

        matchService.setSlots(stageId, assignments);

        return "redirect:/match/fill_group_stage/" + stageId.toString();

    }

    @GetMapping("/fill_playoff_stage/{stageId}")
    public String fillPlayOffStage(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        Stage stage = stageService.getStageById(stageId);
        if (stage.getBestPlace() <= 0) {
            model.addAttribute("error", "Выбран не этап плей-оффа");
            return "redirect:/home";
        }
        StageStatus stageStatus = stageService.getStageStatus(stage);
        if (stageStatus != StageStatus.TEAMS_KNOWN) {
            model.addAttribute("error", "Настройка расписания этапа недоступна");
            return "redirect:/home";
        }
        List<Team> teams = stageService.getTeamsForPlatOffStage(stage);
        List<Match> matches = stage.getMatches();
        List<Slot> availableSlots = slotService.getAllSlotsForStage(stage);
        model.addAttribute("matches", matches);
        model.addAttribute("teams", teams);

        model.addAttribute("slots", availableSlots);
        model.addAttribute("stageId", stageId);
        model.addAttribute("notFilledMatches", matches.size());
        model.addAttribute("matchesCount", (stage.getWorstPlace() - stage.getBestPlace() + 1) / 2 - 1);
        return "match/fill_playoff_stage";
    }


    @PostMapping("/fill_playoff_stage/{stageId}")
    public String fillPlayOffStagePost(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes, @RequestParam Map<String, String> formData) {
        Map<Pair<UUID, UUID>, UUID> assigments = new HashMap<>();
        List<Pair<UUID, UUID>> assigmentsWithNoSlot = new ArrayList<>();
        for (int i = 0; i < formData.size() / 3; i++) {
            String team1Id = formData.get("[" + i + "].team1");
            String team2Id = formData.get("[" + i + "].team2");
            String slotId = formData.get("rows[" + i + "].slot");
                if (slotId != "" && team1Id != "" && team2Id != "") {
                    try {
                        assigments.put(
                                Pair.of(UUID.fromString(team1Id), UUID.fromString(team2Id)),
                                UUID.fromString(slotId)
                        );
                    } catch (IllegalArgumentException e) {

                    }
                    catch (NullPointerException e) {

                    }

                }
                else if (team1Id != "" && team2Id != "") {
                    try {
                        assigmentsWithNoSlot.add(Pair.of(UUID.fromString(team1Id), UUID.fromString(team2Id)));
                    } catch (IllegalArgumentException e) {

                    }
                    catch (NullPointerException e) {

                    }
                }

        }
        matchService.setSlotsForPlayOff(stageId, assigments, assigmentsWithNoSlot);
        return "redirect:/match/fill_playoff_stage/" + stageId.toString();

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
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/referee/{matchId}")
    public String refereeMatch(@PathVariable UUID matchId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(matchId);
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Только судья турнира может судить матчи");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            List<Team> teams = List.of(match.getTeam1(), match.getTeam2());

            List<Map<String, String>> simplifiedTeams = new ArrayList<>();
            for (Team team : teams) {
                Map<String, String> teamMap = new HashMap<>();
                teamMap.put("id", team.getId().toString());
                teamMap.put("name", team.getName());
                simplifiedTeams.add(teamMap);
            }
            model.addAttribute("teams", simplifiedTeams);

            Map<UUID, List<User>> players = matchService.getTeamMembersMap(match.getTeam1(), match.getTeam2());

            Map<String, List<Map<String, String>>> simplifiedPlayersMap = new HashMap<>();
            for (Map.Entry<UUID, List<User>> entry : players.entrySet()) {
                List<Map<String, String>> simplifiedUsers = new ArrayList<>();
                for (User player : entry.getValue()) {
                    Map<String, String> simplifiedUser = new HashMap<>();
                    simplifiedUser.put("id", player.getId().toString());
                    simplifiedUser.put("name", player.getName());
                    simplifiedUsers.add(simplifiedUser);
                }
                simplifiedPlayersMap.put(entry.getKey().toString(), simplifiedUsers);
            }

            List<Goal> goals = goalService.getGoalsByMatch(matchId);
            List<Goal> regularGoals = goals.stream().filter(goal -> !goal.isPenalty()).collect(Collectors.toList());
            List<Goal> penaltyGoals = goals.stream().filter(Goal::isPenalty).collect(Collectors.toList());
            
            // Get after-match penalties
            List<AfterMatchPenalty> afterMatchPenalties = afterMatchPenaltyService.getPenaltiesByMatch(matchId);
            boolean hasAfterMatchPenalties = afterMatchPenaltyService.hasPenalties(matchId);

            model.addAttribute("match", match);
            model.addAttribute("teams", simplifiedTeams);
            model.addAttribute("playersMap", simplifiedPlayersMap);
            model.addAttribute("goals", regularGoals);
            model.addAttribute("penalties", penaltyGoals);
            model.addAttribute("afterMatchPenalties", afterMatchPenalties);
            model.addAttribute("hasAfterMatchPenalties", hasAfterMatchPenalties);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "match/referee_match";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add_goal")
    public String addGoal(@ModelAttribute GoalDTO goalDTO, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(goalDTO.getMatchId());
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Только судья турнира может добавлять голы");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            goalService.addGoal(goalDTO);
            redirectAttributes.addFlashAttribute("success", "Гол успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении гола: " + e.getMessage());
        }

        return "redirect:/match/referee/" + goalDTO.getMatchId().toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add_penalty")
    public String addPenalty(@ModelAttribute GoalDTO goalDTO, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        Match match = matchService.getById(goalDTO.getMatchId());
        UUID tournamentId = match.getStage().getTournament().getId();

        if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
            redirectAttributes.addFlashAttribute("error", "Только судья турнира может добавлять штрафные голы");
            return "redirect:/tournament/view/" + tournamentId.toString();
        }

        try {
            // Set isPenalty to true for penalty goals
            goalDTO.setPenalty(true);
            goalService.addGoal(goalDTO);
            redirectAttributes.addFlashAttribute("success", "Штрафной гол успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении штрафного гола: " + e.getMessage());
        }

        return "redirect:/match/referee/" + goalDTO.getMatchId().toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add_after_match_penalties")
    public String addAfterMatchPenalties(@RequestParam String penalties, @RequestParam UUID matchId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            // Преобразуем JSON строку в список DTO
            ObjectMapper mapper = new ObjectMapper();
            List<AfterMatchPenaltyDTO> penaltyDTOs = mapper.readValue(penalties, 
                mapper.getTypeFactory().constructCollectionType(List.class, AfterMatchPenaltyDTO.class));
            
            if (penaltyDTOs.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Нет данных для сохранения");
                return "redirect:/match/referee/" + matchId.toString();
            }
            
            // Проверяем авторизацию для первого пенальти (все они для одного матча)
            UUID tournamentId = matchService.getById(penaltyDTOs.get(0).getMatchId()).getStage().getTournament().getId();

            if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Только судья турнира может добавлять послематчевые пенальти");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }

            // Сохраняем каждый пенальти
            for (AfterMatchPenaltyDTO penaltyDTO : penaltyDTOs) {
                afterMatchPenaltyService.addPenalties(penaltyDTO);
            }
            
            redirectAttributes.addFlashAttribute("success", "Послематчевые пенальти успешно добавлены");
            return "redirect:/match/referee/" + penaltyDTOs.get(0).getMatchId().toString();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении послематчевых пенальти: " + e.getMessage());
            return "redirect:/match/referee/" + matchId.toString();
        }
    }
}
