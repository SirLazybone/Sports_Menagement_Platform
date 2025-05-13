package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
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
    private final GettingTeamsForPlayOffService gettingTeamsForPlayOffService;
    private final AccessService accessService;
    private final GroupService groupService;

    public MatchController(MatchService matchService, StageService stageService,
                           TournamentService tournamentService, GoalService goalService,
                           SlotService slotService, TeamService teamService, UserService userService,
                           AfterMatchPenaltyService afterMatchPenaltyService, GettingTeamsForPlayOffService gettingTeamsForPlayOffService,
                           AccessService accessService, GroupService groupService) {
        this.matchService = matchService;
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.goalService = goalService;
        this.slotService = slotService;
        this.teamService = teamService;
        this.userService = userService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
        this.gettingTeamsForPlayOffService = gettingTeamsForPlayOffService;
        this.accessService = accessService;
        this.groupService = groupService;
    }


    @GetMapping("/additional/{tournamentId}")
    public String additionalMatches(@PathVariable UUID tournamentId, Model model, @AuthenticationPrincipal User user) {
        List<Match> matchesInWork = matchService.getAdditionalMatchesInWork(tournamentId);
        List<Match> publishedMatches = matchService.getPublishedAdditionalMatches(tournamentId);
        List<Match> finishedMatches = matchService.getFinishedAdditionalMatches(tournamentId);
        List<Slot> freeSlots = slotService.getFreeSlots(tournamentService.getById(tournamentId));


        boolean isUserOrg = false;
        boolean isUserChiefOrg = false;

        if (user != null) {
            List<UserOrgCom> userOrgComList = tournamentService.getById(tournamentId).getUserOrgCom().getOrgCom().getUserOrgComList().stream().filter(userOrgCom -> userOrgCom.getUser().getId().equals(user.getId())).collect(Collectors.toList());
            if (userOrgComList.size() != 0) {
                isUserOrg = true;
                if (userOrgComList.get(0).getOrgRole() == Org.CHIEF) isUserChiefOrg = true;
            }
        }


        model.addAttribute("isUserOrg", isUserOrg);
        model.addAttribute("isUserChiefOrg", isUserChiefOrg);

        model.addAttribute("matchesInWork", matchesInWork);
        model.addAttribute("publishedMatches", publishedMatches);
        model.addAttribute("finishedMatches", finishedMatches);
        model.addAttribute("freeSlots", freeSlots);
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("additionalMatchDTO", new AdditionalMatchDTO());
        model.addAttribute("teams", tournamentService.getAllTeamsByTournamentId(tournamentId));



        return "/match/additional_matches";

    }

    @PostMapping("/new_additional/{tournamentId}")
    public String additionalMatchesPost(@PathVariable UUID tournamentId, Model model, @AuthenticationPrincipal User user, @ModelAttribute AdditionalMatchDTO additionalMatchDTO) {
        try {
            tournamentService.getById(tournamentId);
            matchService.createAdditionalMatch(tournamentId, additionalMatchDTO);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Чемпионат не найден");
        }
        return "redirect:/match/additional/" + tournamentId.toString();
    }



    @GetMapping("/fill_group_stage/{stageId}")
    public String fillGroupStage(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user) {
        try {
            Stage stage = stageService.getStageById(stageId);
            Tournament tournament = stage.getTournament();
            
            if (!accessService.isUserOrgOfTournament(user.getId(), tournament.getId())) {
                throw new AccessDeniedException("У вас нет доступа");
            }

            // Get all teams for the tournament
            List<Team> teams = tournamentService.getAllTeamsByTournamentId(tournament.getId());
            List<Pair<UUID, String>> teamsPairs = teams.stream()
                .map(team -> Pair.of(team.getId(), team.getName()))
                .collect(Collectors.toList());

            // Get groups and their teams
            List<Group> groups = groupService.getGroups(tournament.getId());
            Map<String, List<Pair<UUID, String>>> groupsMap = new TreeMap<>(); // Using TreeMap for natural sorting
            for (Group group : groups) {
                List<Pair<UUID, String>> teamsPair = group.getTeams().stream()
                    .map(team -> Pair.of(team.getId(), team.getName()))
                    .collect(Collectors.toList());
                groupsMap.put(group.getName(), teamsPair);
            }

            // Get matches for each group
            Map<Group, List<Match>> matches = matchService.createGroupMatchIfNotCreated(stageId);
            // Sort matches by group name
            Map<Group, List<Match>> sortedMatches = new TreeMap<>(Comparator.comparing(Group::getName));
            sortedMatches.putAll(matches);
            
            // Get available slots
            List<Slot> slots = slotService.getAllSlotsForStage(stage);

            model.addAttribute("stage", stage);
            model.addAttribute("tournament", tournament);
            model.addAttribute("tournamentId", tournament.getId());
            model.addAttribute("stageId", stageId);
            model.addAttribute("isPublished", stage.isPublished());
            model.addAttribute("isUserChief", tournamentService.isUserChiefOfTournament(user.getId(), tournament.getId()));
            model.addAttribute("matches", sortedMatches);
            model.addAttribute("slot", slots);
            model.addAttribute("teams", teamsPairs);
            model.addAttribute("groups", groupsMap);
            model.addAttribute("newGroupName", "");

            return "match/fill_group";
        } catch (RuntimeException e) {
            System.out.println("ОШИБКА: " + e.getMessage());
            throw new ResourceNotFoundException("Этап не найден");
        }
    }

    @PostMapping("/fill_group_stage/{stageId}")
    public String fillGroupStagePost(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes, @RequestParam Map<String, String> slotAssignments) {
        try {
            Stage stage = stageService.getStageById(stageId);
            if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
                redirectAttributes.addFlashAttribute("error", "У вас нет доступа для управления матчами");
                return "redirect:/home";
            }

            // Получаем все матчи этапа
            Map<Group, List<Match>> allMatches = matchService.createGroupMatchIfNotCreated(stageId);
            
            // Создаем карту для отслеживания слотов команд
            Map<UUID, Map<UUID, Match>> teamSlots = new HashMap<>(); // teamId -> (slotId -> match)

            // Сначала собираем существующие назначения
            for (List<Match> matches : allMatches.values()) {
                for (Match match : matches) {
                    if (match.getSlot() != null) {
                        UUID slotId = match.getSlot().getId();
                        for (UUID teamId : Arrays.asList(match.getTeam1().getId(), match.getTeam2().getId())) {
                            teamSlots.computeIfAbsent(teamId, k -> new HashMap<>()).put(slotId, match);
                        }
                    }
                }
            }

            // Проверяем новые назначения на конфликты
            for (Map.Entry<String, String> entry : slotAssignments.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                
                String matchIdStr = entry.getKey().substring(
                    entry.getKey().indexOf('[') + 1,
                    entry.getKey().indexOf(']')
                );
                UUID matchId = UUID.fromString(matchIdStr);
                UUID newSlotId = UUID.fromString(entry.getValue());

                // Находим матч
                Match currentMatch = null;
                for (List<Match> matches : allMatches.values()) {
                    for (Match match : matches) {
                        if (match.getId().equals(matchId)) {
                            currentMatch = match;
                            break;
                        }
                    }
                    if (currentMatch != null) break;
                }

                if (currentMatch != null) {
                    // Проверяем конфликты для обеих команд
                    for (UUID teamId : Arrays.asList(currentMatch.getTeam1().getId(), currentMatch.getTeam2().getId())) {
                        Map<UUID, Match> teamExistingSlots = teamSlots.getOrDefault(teamId, new HashMap<>());
                        
                        // Проверяем, не назначен ли уже слот для этой команды
                        if (teamExistingSlots.containsKey(newSlotId)) {
                            Match conflictingMatch = teamExistingSlots.get(newSlotId);
                            if (!conflictingMatch.getId().equals(currentMatch.getId())) {
                                redirectAttributes.addFlashAttribute("error", 
                                    String.format("Конфликт: команда %s уже играет в это время в матче %s vs %s",
                                        currentMatch.getTeam1().getId().equals(teamId) ? currentMatch.getTeam1().getName() : currentMatch.getTeam2().getName(),
                                        conflictingMatch.getTeam1().getName(),
                                        conflictingMatch.getTeam2().getName()));
                                return "redirect:/match/fill_group_stage/" + stageId.toString();
                            }
                        }
                        
                        // Добавляем новое назначение
                        teamExistingSlots.put(newSlotId, currentMatch);
                        teamSlots.put(teamId, teamExistingSlots);
                    }
                }
            }

            // Преобразуем назначения слотов
            Map<UUID, UUID> assignments = slotAssignments.entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(
                            entry -> UUID.fromString(entry.getKey().substring(
                                    entry.getKey().indexOf('[') + 1,
                                    entry.getKey().indexOf(']'))),
                            entry -> UUID.fromString(entry.getValue())
                    ));

            if (!assignments.isEmpty()) {
                matchService.setSlots(stageId, assignments);
                redirectAttributes.addFlashAttribute("success", "Слоты успешно назначены");
            } else {
                redirectAttributes.addFlashAttribute("info", "Не было выбрано ни одного слота для назначения");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при назначении слотов: " + e.getMessage());
        }

        return "redirect:/match/fill_group_stage/" + stageId.toString();
    }

    @GetMapping("/fill_playoff_stage/{stageId}")
    public String fillPlayOffStage(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        Stage stage;
        try {
            stage = stageService.getStageById(stageId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Этап не найден");
        }
        boolean isUserOrg = false;
        boolean isUserChief = false;
        try {
            isUserOrg = accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId());
            if (!isUserOrg) {
                throw new AccessDeniedException("У вас нет доступа");
            }
            isUserChief = accessService.isUserChiefOfTournament(user.getId(), stage.getTournament().getId());
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        if (stage.getBestPlace() <= 0) {
            model.addAttribute("error", "Выбран не этап плей-оффа");
            return "redirect:/home";
        }
        StageStatus stageStatus = stageService.getStageStatus(stage);
        if (stageStatus != StageStatus.TEAMS_KNOWN) {
            model.addAttribute("error", "Настройка расписания этапа недоступна");
            return "redirect:/home";
        }
        List<Team> teams = gettingTeamsForPlayOffService.getTeamsForPlayOffStage(stage);
        List<Match> matches = stage.getMatches();
        
        // Create empty matches if they don't exist or if there are fewer matches than needed
        int maxMatches = stage.getWorstPlace() / 2;
        if (matches.size() < maxMatches) {
            for (int i = matches.size(); i < maxMatches; i++) {
                Match match = Match.builder()
                    .stage(stage)
                    .isResultPublished(false)
                    .build();
                matches.add(match);
            }
        }
        
        List<Slot> availableSlots = slotService.getAllSlotsForStage(stage);
        
        // Add success/error messages if they exist
        if (redirectAttributes.getFlashAttributes().containsKey("success")) {
            model.addAttribute("success", redirectAttributes.getFlashAttributes().get("success"));
        }
        if (redirectAttributes.getFlashAttributes().containsKey("error")) {
            model.addAttribute("error", redirectAttributes.getFlashAttributes().get("error"));
        }
        
        model.addAttribute("matches", matches);
        model.addAttribute("teams", teams);
        model.addAttribute("slots", availableSlots);
        model.addAttribute("stageId", stageId);
        model.addAttribute("isPublished", stage.isPublished());
        model.addAttribute("isUserChief", isUserChief);
        model.addAttribute("isUserOrg", isUserOrg);

        return "match/fill_playoff_stage";
    }


    @PostMapping("/fill_playoff_stage/{stageId}")
    public String fillPlayOffStagePost(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes, @RequestParam Map<String, String> formData) {
        try {
            Stage stage = stageService.getStageById(stageId);
            int maxMatches = stage.getWorstPlace() / 2;
            Map<Pair<UUID, UUID>, UUID> assignments = new HashMap<>();
            List<Pair<UUID, UUID>> assignmentsWithNoSlot = new ArrayList<>();

            for (int i = 0; i < maxMatches; i++) {
                String team1Id = formData.get("[" + i + "].team1");
                String team2Id = formData.get("[" + i + "].team2");
                String slotId = formData.get("rows[" + i + "].slot");

                // Skip if both teams are empty
                if ((team1Id == null || team1Id.isEmpty()) && (team2Id == null || team2Id.isEmpty())) {
                    continue;
                }

                // Validate that both teams are selected if either is selected
                if ((team1Id == null || team1Id.isEmpty()) != (team2Id == null || team2Id.isEmpty())) {
                    redirectAttributes.addFlashAttribute("error", "Для каждого матча должны быть выбраны обе команды");
                    return "redirect:/match/fill_playoff_stage/" + stageId.toString();
                }

                try {
                    UUID team1 = UUID.fromString(team1Id);
                    UUID team2 = UUID.fromString(team2Id);

                    // Check if teams are different
                    if (team1.equals(team2)) {
                        redirectAttributes.addFlashAttribute("error", "Команда не может играть сама с собой");
                        return "redirect:/match/fill_playoff_stage/" + stageId.toString();
                    }

                    if (slotId != null && !slotId.isEmpty()) {
                        assignments.put(Pair.of(team1, team2), UUID.fromString(slotId));
                    } else {
                        assignmentsWithNoSlot.add(Pair.of(team1, team2));
                    }
                } catch (IllegalArgumentException e) {
                    redirectAttributes.addFlashAttribute("error", "Некорректные данные матча");
                    return "redirect:/match/fill_playoff_stage/" + stageId.toString();
                }
            }

            matchService.setSlotsForPlayOff(stageId, assignments, assignmentsWithNoSlot);
            redirectAttributes.addFlashAttribute("success", "Матчи успешно сохранены");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при сохранении матчей: " + e.getMessage());
        }
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
            System.out.println("Match ID : " + goalDTO.getMatchId());
            System.out.println("Time : " + goalDTO.getTime());
            System.out.println("Team ID : " + goalDTO.getTeamId());
            System.out.println("Match ID : " + goalDTO.getTime());


            goalService.addGoal(goalDTO);
            redirectAttributes.addFlashAttribute("success", "Гол успешно добавлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении гола: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace()));
        }

        return "redirect:/match/view/" + goalDTO.getMatchId().toString();
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
                return "redirect:/match/view/" + matchId.toString();
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
            return "redirect:/match/view/" + penaltyDTOs.get(0).getMatchId().toString();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении послематчевых пенальти: " + e.getMessage());
            return "redirect:/match/view/" + matchId.toString();
        }
    }

    @PostMapping("/publish/{matchId}")
    public String publishResults(@PathVariable("matchId") UUID matchId, Model model) {
        try {
            Match match = matchService.getById(matchId);
            matchService.publishResult(matchId);
        } catch (RuntimeException e) {
            model.addAttribute("error", "Ошибка при опубликовывании результатов матча: " + e.getMessage());
            throw new ResourceNotFoundException("Матч не найден");
        }
        return "redirect:/match/view/" + matchId.toString();
    }

    @GetMapping("/view/{matchId}")
    public String showMatch(@PathVariable("matchId") UUID matchId, Model model, @AuthenticationPrincipal User user) {
        try {
            Match match = matchService.getById(matchId);
            Stage stage = match.getStage();
            Tournament tournament = match.getStage().getTournament();
            boolean isRef = false;
            boolean isOrg = false;
            boolean isCap1 = false;
            boolean isCap2 = false;

            try {
                isRef = accessService.isUserRefOfTournament(user.getId(), tournament.getId());
            } catch (RuntimeException ignored) {
            }

            try {
                isOrg = accessService.isUserChiefOfTournament(user.getId(), tournament.getId());
            } catch (RuntimeException ignored) {
            }

            try {
                isCap1 = accessService.isUserCapOfTeam(user.getId(), match.getTeam1().getId());
            } catch (RuntimeException ignored) {
            }

            try {
                isCap2 = accessService.isUserCapOfTeam(user.getId(), match.getTeam2().getId());
            } catch (RuntimeException ignored) {
            }

            List<Goal> goals = goalService.getGoalsByMatch(matchId);
            List<Goal> team1Goals = goals.stream()
                .filter(x -> x.getTeam().getId().equals(match.getTeam1().getId()))
                .collect(Collectors.toList());
            List<Goal> team2Goals = goals.stream()
                .filter(x -> x.getTeam().getId().equals(match.getTeam2().getId()))
                .collect(Collectors.toList());

            List<AfterMatchPenalty> afterMatchPenalties = afterMatchPenaltyService.getPenaltiesByMatch(matchId);
            List<AfterMatchPenalty> team1Penalties = afterMatchPenalties.stream()
                .filter(x -> x.getTeam().getId().equals(match.getTeam1().getId()))
                .collect(Collectors.toList());
            List<AfterMatchPenalty> team2Penalties = afterMatchPenalties.stream()
                .filter(x -> x.getTeam().getId().equals(match.getTeam2().getId()))
                .collect(Collectors.toList());

            Map<UUID, List<User>> players = matchService.getTeamMembersMap(match.getTeam1(), match.getTeam2());
            List<Slot> availableSlots = slotService.getAllNotInUse();

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

            List<Map<String, String>> simplifiedTeams = new ArrayList<>();
            Map<String, String> team1Map = new HashMap<>();
            team1Map.put("id", match.getTeam1().getId().toString());
            team1Map.put("name", match.getTeam1().getName());
            simplifiedTeams.add(team1Map);
            
            Map<String, String> team2Map = new HashMap<>();
            team2Map.put("id", match.getTeam2().getId().toString());
            team2Map.put("name", match.getTeam2().getName());
            simplifiedTeams.add(team2Map);

            if (stage.getTournament().getSport().equals(Sport.VOLLEYBALL)) {
                int team1sets = 0;
                int team2sets = 0;

                for (int i = 0; i < 5; i++) {
                    int setNum = i;
                    int count1 = goals.stream().filter(x -> x.getSet_number() == setNum + 1 && x.getTeam().getId().equals(match.getTeam1().getId())).toList().size();
                    int count2 = goals.stream().filter(x -> x.getSet_number() == setNum + 1 && x.getTeam().getId().equals(match.getTeam2().getId())).toList().size();
                    if (count1 > count2) {
                        team1sets++;
                    } else {
                        team2sets++;
                    }
                }
                model.addAttribute("team1sets", team1sets);
                model.addAttribute("team2sets", team2sets);
            }

            model.addAttribute("match", match);
            model.addAttribute("stage", stage);
            model.addAttribute("tournament", tournament);
            model.addAttribute("isRef", isRef);
            model.addAttribute("isOrg", isOrg);
            model.addAttribute("isCap1", isCap1);
            model.addAttribute("isCap2", isCap2);
            model.addAttribute("team1Goals", team1Goals);
            model.addAttribute("team2Goals", team2Goals);
            model.addAttribute("team1Penalties", team1Penalties);
            model.addAttribute("team2Penalties", team2Penalties);
            model.addAttribute("players", simplifiedPlayersMap);
            model.addAttribute("teams", simplifiedTeams);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("goalDTO", new GoalDTO());
            model.addAttribute("afterMatchPenaltyDTO", new AfterMatchPenaltyDTO());

            return "match/view";
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Матч не найден");
        }
    }

    @PostMapping("/withdraw/{matchId}")
    public String withdrawFromMatch(@PathVariable UUID matchId, @RequestParam UUID teamId, 
                                  @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) {
        boolean isCap1 = false;
        boolean isCap2 = false;
        Match match = matchService.getById(matchId);
        try {
            isCap1 = accessService.isUserCapOfTeam(user.getId(), match.getTeam1().getId());
        } catch (RuntimeException ignored) {}
        try {
            isCap2 = accessService.isUserCapOfTeam(user.getId(), match.getTeam2().getId());
        } catch (RuntimeException ignored) {}
        try {
            if (!isCap1 && !isCap2) {
                redirectAttributes.addFlashAttribute("error", "Only team captains can withdraw from a match");
                return "redirect:/match/view/" + matchId.toString();
            }
            
            if (!teamId.equals(match.getTeam1().getId()) && !teamId.equals(match.getTeam2().getId())) {
                redirectAttributes.addFlashAttribute("error", "Invalid team");
                return "redirect:/match/view/" + matchId.toString();
            }
            
            GoalDTO goalDTO = new GoalDTO();
            goalDTO.setMatchId(matchId);
            goalDTO.setTeamId(teamId);
            goalDTO.setTime(-1);
            goalDTO.setPlayerId(user.getId());
            
            goalService.addGoal(goalDTO);
            matchService.publishResult(matchId);
            
            redirectAttributes.addFlashAttribute("success", "Team has withdrawn from the match");
            return "redirect:/match/view/" + matchId.toString();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing withdrawal: " + e.getMessage());
            return "redirect:/match/view/" + matchId.toString();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete_goal")
    public String deleteGoal(@RequestParam UUID goalId, @RequestParam UUID matchId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            Match match = matchService.getById(matchId);
            UUID tournamentId = match.getStage().getTournament().getId();
            if (!tournamentService.isUserRefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Только судья турнира может удалять голы");
                return "redirect:/tournament/view/" + tournamentId;
            }
            goalService.deleteGoal(goalId);
            redirectAttributes.addFlashAttribute("success", "Гол успешно удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении гола: " + e.getMessage());
        }
        return "redirect:/match/view/" + matchId;
    }

    
}
