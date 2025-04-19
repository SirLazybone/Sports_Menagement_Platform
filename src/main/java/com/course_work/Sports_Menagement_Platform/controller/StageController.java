package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
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

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("stage")
public class StageController {
    private final StageService stageService;
    private final TournamentService tournamentService;
    private final MatchService matchService;

    private final GroupService groupService;

    public StageController(StageService stageService, TournamentService tournamentService,
                           MatchService matchService, GroupService groupService) {
        this.stageService = stageService;
        this.tournamentService = tournamentService;
        this.matchService = matchService;
        this.groupService = groupService;
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
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/stage/create_stage/" + tournamentId.toString();
    }

    @GetMapping("/manage_groups/{tournamentId}")
    public String manageGroups(@PathVariable UUID tournamentId, Model model, RedirectAttributes redirectAttributes, @AuthenticationPrincipal User user) {
        // TODO: проверка

        List<Group> groups = groupService.getGroups(tournamentId);
        List<Team> teams = tournamentService.getAllTeamsByTournamentId(tournamentId);
        Map<String, List<Pair<UUID, String>>> groupsMap = new HashMap<>();
        for (Group group : groups) {
            List<Pair<UUID, String>> teamsPair = group.getTeams().stream().map(i -> Pair.of(i.getId(), i.getName())).collect(Collectors.toList());
            groupsMap.put(group.getName(), teamsPair);
        }
        List<Pair<UUID, String>> teamsPairs = teams.stream().map(i -> Pair.of(i.getId(), i.getName())).collect(Collectors.toList());
        model.addAttribute("teams", teamsPairs);
        model.addAttribute("groups", groupsMap);
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("newGroupName", "");
        model.addAttribute("stageId", stageService.getGroupStage(tournamentId));

        return "stage/manage_groups";

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create_groups/{tournamentId}")
    public String createGroups(@PathVariable UUID tournamentId, @RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes,
                             @AuthenticationPrincipal User user) {

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

        groupService.updateGroupTeams(stage.getId(), groups);


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
}
