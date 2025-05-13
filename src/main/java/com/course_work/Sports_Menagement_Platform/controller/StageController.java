package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GroupService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import jakarta.validation.Valid;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("stage")
public class StageController {
    private final StageService stageService;
    private final TournamentService tournamentService;
    private final MatchService matchService;

    private final GroupService groupService;
    private final AccessService accessService;

    public StageController(StageService stageService, TournamentService tournamentService,
                           MatchService matchService, GroupService groupService,
                           AccessService accessService) {
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.matchService = matchService;
        this.groupService = groupService;
        this.accessService = accessService;
    }

    // Разводящий метод для стейжей
    @GetMapping("/view/{stageId}")
    public String viewStage(@PathVariable UUID stageId, Model model, @AuthenticationPrincipal User user) {
        Stage stage;
        try {
            stage = stageService.getStageById(stageId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Этап не найден");
        }
        if (stage.isPublished()) {
            return "redirect:/stage/info/" + stage.getId().toString();
        } else {
            try {
                if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
                    throw new AccessDeniedException("Пока этап не опубликован, у вас нет доступа к просмотру");
                }
            } catch (RuntimeException e) {
                throw new AccessDeniedException("Пока этап не опубликован, у вас нет доступа к просмотру");
            }
        }
        if (stage.getBestPlace() == 0) {
            return "redirect:/match/fill_group_stage/" + stage.getId().toString();
        }
        else if (stage.getBestPlace() > 0) {
            return "redirect:/match/fill_playoff_stage/" + stage.getId().toString();
        }
        else {
            return "";
        }
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

        Tournament tournament = tournamentService.getById(tournamentId);

        model.addAttribute("stage", new StageCreationDTO());
        model.addAttribute("tournament", tournament);
        model.addAttribute("tournamentId", tournamentId);
        List<Stage> list = stageService.getStagesByTournament(tournamentId);
        if (list == null || list.isEmpty()) {
            model.addAttribute("error", "Этапы пока не созданы или не опубликованы");
        }
        model.addAttribute("stages", list);
        
        // Find group stage if exists
        Stage groupStage = list.stream()
                .filter(stage -> stage.getBestPlace() == 0 && stage.getWorstPlace() == 0)
                .findFirst()
                .orElse(null);
        model.addAttribute("groupStage", groupStage);
        
        return "stage/create_stage";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_stage/{tournamentId}")
    public String createStage(@PathVariable UUID tournamentId, @Valid @ModelAttribute("stage") StageCreationDTO stageDTO, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            tournamentService.getById(tournamentId);
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create stage");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }

            model.addAttribute("tournamentId", tournamentId);
            if (bindingResult.hasErrors()) {
                return "stage/create_stage";
            }

            stageService.createStage(stageDTO, tournamentId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Чемпионат не найден");
        }
        return "redirect:/stage/create_stage/" + tournamentId.toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_group_stage/{tournamentId}")
    public String createGroupStage(@PathVariable UUID tournamentId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can create group stage");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }

            // Check if group stage already exists
            List<Stage> stages = stageService.getStagesByTournament(tournamentId);
            boolean groupStageExists = stages.stream()
                    .anyMatch(stage -> stage.getBestPlace() == 0 && stage.getWorstPlace() == 0);

            if (groupStageExists) {
                redirectAttributes.addFlashAttribute("error", "Group stage already exists");
                return "redirect:/stage/create_stage/" + tournamentId.toString();
            }

            // Create group stage
            StageCreationDTO stageDTO = new StageCreationDTO();
            stageDTO.setBestPlace(0);
            stageDTO.setWorstPlace(0);
            stageService.createStage(stageDTO, tournamentId);
            redirectAttributes.addFlashAttribute("success", "Group stage created successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/stage/create_stage/" + tournamentId.toString();
    }

    @GetMapping("/stages/{tournamentId}")
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
            return "redirect:/tournament/view/" + tournamentId.toString();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            Stage stage = stageService.getStageById(stageId);
            if (stage.getBestPlace() == 0) {
                return "redirect:/match/fill_group_stage/" + stageId.toString();
            } else {
                return "redirect:/match/fill_playoff_stage/" + stageId.toString();
            }
        }
    }

    @GetMapping("/manage_groups/{tournamentId}")
    public String manageGroups(@PathVariable UUID tournamentId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        System.out.println("Entering manageGroups for tournamentId: " + tournamentId);
        try {
            Tournament tournament = tournamentService.getById(tournamentId);
            System.out.println("Found tournament: " + tournament.getName());
            
            // Check access rights
            boolean isUserOrg = accessService.isUserOrgOfTournament(user.getId(), tournamentId);
            System.out.println("Is user org: " + isUserOrg);
            if (!isUserOrg) {
                throw new AccessDeniedException("У вас нет доступа к управлению группами");
            }
            
            // Check registration status
            Stage groupStage = stageService.createGroupStageIfNotExists(tournamentId);
            System.out.println("Group stage created/exists: " + (groupStage != null));
            if (groupStage == null) {
                if (tournament.getRegisterDeadline().isAfter(LocalDate.now())) {
                    System.out.println("Registration deadline not passed");
                    model.addAttribute("error", "Регистрация еще не завершена. Группы можно будет настроить после " + tournament.getRegisterDeadline());
                } else {
                    System.out.println("Has pending applications");
                    model.addAttribute("error", "Есть ожидающие заявки на участие. Группы можно будет настроить после их рассмотрения");
                }
                model.addAttribute("registrationNotFinished", true);
                return "stage/manage_groups";
            }
            
            // Get groups and teams
            List<Group> groups = groupService.getGroups(tournamentId);
            List<Team> teams = tournamentService.getAllTeamsByTournamentId(tournamentId);
            System.out.println("Found " + groups.size() + " groups and " + teams.size() + " teams");
            
            // Prepare data for view
            Map<String, List<Pair<UUID, String>>> groupsMap = groups.stream()
                .collect(Collectors.toMap(
                    Group::getName,
                    group -> group.getTeams().stream()
                        .map(team -> Pair.of(team.getId(), team.getName()))
                        .collect(Collectors.toList())
                ));
            System.out.println("DEBUG: Groups map keys: " + groupsMap.keySet());
            System.out.println("DEBUG: Groups map values: " + groupsMap.values());
                
            List<Pair<UUID, String>> teamsPairs = teams.stream()
                .map(team -> Pair.of(team.getId(), team.getName()))
                .collect(Collectors.toList());
                
            // Add attributes to model
            model.addAttribute("teams", teamsPairs);
            model.addAttribute("groups", groupsMap);
            model.addAttribute("tournamentId", tournamentId);
            model.addAttribute("newGroupName", "");
            model.addAttribute("stageId", groupStage.getId());
            
            // Add flash attributes if they exist
            if (redirectAttributes.getFlashAttributes().containsKey("error")) {
                model.addAttribute("error", redirectAttributes.getFlashAttributes().get("error"));
            }
            if (redirectAttributes.getFlashAttributes().containsKey("success")) {
                model.addAttribute("success", redirectAttributes.getFlashAttributes().get("success"));
            }
            
            return "stage/manage_groups";
            
        } catch (AccessDeniedException e) {
            System.out.println("Access denied: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tournament/view/" + tournamentId;
        } catch (RuntimeException e) {
            System.out.println("Runtime exception: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка: " + e.getMessage());
            return "redirect:/tournament/view/" + tournamentId;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_groups/{tournamentId}")
    public String createGroups(@PathVariable UUID tournamentId, @RequestParam Map<String, String> allParams, 
                             RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {

        if (stageService.createGroupStageIfNotExists(tournamentId) == null) {
            redirectAttributes.addFlashAttribute("error", "Регистрация еще не завершена, настройка групп недоступна");
            return "redirect:/stage/manage_groups/" + tournamentId.toString();
        }

        // Extract group names and validate for duplicates
        Set<String> groupNames = allParams.keySet().stream()
                .filter(k -> k.startsWith("groupNames["))
                .map(k -> k.substring("groupNames[".length(), k.length() - 1))
                .collect(Collectors.toSet());
                

        Map<String, List<UUID>> groups = new HashMap<>();
        
        for (String groupName : groupNames) {
            List<UUID> teamIds = allParams.keySet().stream()
                    .filter(k -> k.startsWith("groupTeams[" + groupName + "]"))
                    .map(k -> UUID.fromString(allParams.get(k)))
                    .collect(Collectors.toList());
                    
            
            groups.put(groupName, teamIds);
        }

        Stage stage = stageService.getGroupStage(tournamentId);

        try {
            groupService.updateGroupTeams(stage.getId(), groups);
            matchService.createGroupMatchIfNotCreated(stage.getId());
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Одна команда может быть только в одной группе")) {
                redirectAttributes.addFlashAttribute("error", "Одна команда может быть только в одной группе");
            } else if (e.getMessage().equals("Нельзя создать группу с одной командой")) {
                redirectAttributes.addFlashAttribute("error", "Нельзя создать группу с одной командой");
            } else {
                redirectAttributes.addFlashAttribute("error", "Произошла ошибка при сохранении групп: " + e.getMessage());
            }
            return "redirect:/stage/manage_groups/" + tournamentId.toString();
        }

        redirectAttributes.addFlashAttribute("success", "Группы успешно сохранены");
        return "redirect:/stage/manage_groups/" + tournamentId.toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add_teams_to_group/{stageId}")
    public String addTeamsToGroup(@PathVariable UUID stageId, @RequestParam String name, 
                                @RequestParam List<UUID> teamIds, RedirectAttributes redirectAttributes, 
                                @AuthenticationPrincipal User user) {
        try {
            UUID tournamentId = stageService.getTournamentByStage(stageId).getId();
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can manage groups");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }

            GroupDTO groupDTO = new GroupDTO();
            groupDTO.setStageId(stageId);
            groupDTO.setName(name);
            groupDTO.setTeamIds(teamIds.stream().filter(id -> id != null).collect(Collectors.toList()));

            stageService.addTeamsToGroup(groupDTO);
            redirectAttributes.addFlashAttribute("success", "Teams added to group successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stage/manage_groups/" + stageId.toString();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove_team_from_group/{groupId}/{teamId}")
    public String removeTeamFromGroup(@PathVariable UUID groupId, @PathVariable UUID teamId,
                                    RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        try {
            Stage stage = stageService.getStageByGroup(groupId);
            UUID tournamentId = stage.getTournament().getId();
            
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                redirectAttributes.addFlashAttribute("error", "Only chief of the tournament can manage groups");
                return "redirect:/tournament/view/" + tournamentId.toString();
            }

            stageService.removeTeamFromGroup(groupId, teamId);
            redirectAttributes.addFlashAttribute("success", "Team removed from group successfully");
            return "redirect:/stage/manage_groups/" + stage.getId().toString();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/info/{stageId}")
    public String showInfo(@PathVariable UUID stageId, Model model) {
        try {
            Stage stage = stageService.getStageById(stageId);
            Map<Group, List<Match>> groupMatches = new HashMap<>();
            Map<UUID, List<Match>> matchesMap = new HashMap<>();
            if (stage.getBestPlace() == 0) {
                groupMatches = matchService.createGroupMatchIfNotCreated(stage.getId());
            } else {
                matchesMap = matchService.getMatchesByStagesMap(List.of(stage));
            }
            model.addAttribute("stage", stage);
            model.addAttribute("groups", groupMatches);
            model.addAttribute("matches", matchesMap.get(stage.getId()));
            model.addAttribute("tournament", stage.getTournament());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Этап не найден");
        }
        return "stage/view";
    }
}


