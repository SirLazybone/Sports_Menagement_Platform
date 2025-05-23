package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.FileStorageService;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/team")
public class TeamController {
    private final UserService userService;
    private final TeamService teamService;
    private final FileStorageService fileStorageService;
    private final AccessService accessService;
    private final TournamentService tournamentService;

    public TeamController(UserService userService, TeamService teamService,
                          FileStorageService fileStorageService, AccessService accessService,
                          TournamentService tournamentService) {
        this.userService = userService;
        this.teamService = teamService;
        this.fileStorageService = fileStorageService;
        this.accessService = accessService;
        this.tournamentService = tournamentService;
    }

    @GetMapping("/new")
    public String newTeam(Model model) {
        model.addAttribute("team", new TeamDTO());
        model.addAttribute("sports", new SportDTO());
        return "team/new_team";
    }

    @PostMapping("/create")
    public String createTeam(@Valid @ModelAttribute("team") TeamDTO team, 
                           BindingResult bindingResult, 
                           Model model, 
                           @AuthenticationPrincipal User user,
                           @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        if (bindingResult.hasErrors()) {
            return "team/new_team";
        }

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(logoFile);
                team.setLogo(fileName);
            }
            Team newTeam = teamService.createTeam(team, user);
            return "redirect:/team/view/" + newTeam.getId().toString();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "team/new_team";
        }
    }

    @GetMapping("show_all")
    public String showAll(Model model, @AuthenticationPrincipal User user) {
        List<Team> list;
        try {
            list = teamService.getAllActiveTeamByUser(user);
            model.addAttribute("teams", list);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "team/show_all";
    }

    @GetMapping("/view/{teamId}")
    public String viewTeam(@PathVariable("teamId") UUID teamId, Model model, @AuthenticationPrincipal User user) {
        Team team;
        try {
            team = teamService.getById(teamId);
            model.addAttribute("teamId", teamId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Такой команды нет");
        }
        try {
            List<UserTeamDTO> list = teamService.getAllUserByTeamDTO(teamId);
            model.addAttribute("members", list);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        try {
            List<TeamTournament> acceptedTeamTournaments = tournamentService.getAcceptedTeamTournament(teamId);
            List<TeamTournament> otherTeamTournaments = tournamentService.getOtherTeamTournament(teamId);
            model.addAttribute("acceptedTeamTournaments", acceptedTeamTournaments);
            model.addAttribute("otherTeamTournaments", otherTeamTournaments);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("teamId", team.getId());
        model.addAttribute("userId",  user != null ? user.getId() : "");

        model.addAttribute("team", TeamDTO.builder()
                .name(team.getName())
                .sport(team.getSport())
                .logo(team.getLogo())
                .build());
        model.addAttribute("invitationStatuses", new InvitationStatusDTO());

        boolean isCap = false;
        boolean isUserMember = false;
        boolean isOnlyActiveCaptain = false;
        try {
            isCap = accessService.isUserCapOfTeam(user.getId(), teamId);
            isUserMember = accessService.isUserMemberOfTeam(user.getId(), teamId);
            isOnlyActiveCaptain = teamService.isOnlyActiveCaptain(teamId, user.getId());
        } catch (RuntimeException ignored) {}
        model.addAttribute("isCap", isCap);
        model.addAttribute("isUserMember", isUserMember);
        model.addAttribute("isOnlyActiveCaptain", isOnlyActiveCaptain);
        model.addAttribute("today", LocalDate.now());
        return "team/view";
    }

    @GetMapping("/create_invitation/{teamId}")
    public String createInvitation(@PathVariable UUID teamId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserCapOfTeam(user.getId(), teamId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("invitation", new InvitationTeamDTO());
        model.addAttribute("teamId", teamId);
        return "team/new_invitation";
    }

    @PostMapping("/send_invitation/{teamId}")
    public String sendInvitation(@PathVariable UUID teamId, @Valid @ModelAttribute("invitation") InvitationTeamDTO invitationTeamDTO, BindingResult bindingResult, Model model) {
        model.addAttribute("teamId", teamId);
        if (bindingResult.hasErrors()) {
            return "team/new_invitation";
        }

        try {
            teamService.createInvitation(teamId, invitationTeamDTO.getIsCap(), invitationTeamDTO.getNotPlaying(), userService.findByTel(invitationTeamDTO.getTel()));
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "team/new_invitation";
        } catch (Exception e) {
            model.addAttribute("error", "smth really wrong: " + e.getMessage());
            return "team/new_invitation";
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @PostMapping("/kick/{teamId}/{userTel}")
    public String kickUser(@PathVariable UUID teamId, @PathVariable String userTel, Model model) {
        try {
            User user = userService.findByTel(userTel);
            teamService.kickUser(teamId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @PostMapping("/cancel/{teamId}/{userTel}")
    public String cancelInvitation(@PathVariable UUID teamId, @PathVariable String userTel, Model model) {
        try {
            User user = userService.findByTel(userTel);
            teamService.cancelInvitation(teamId, user.getId());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @PostMapping("/leave/{teamId}")
    public String leaveTeam(@PathVariable UUID teamId, @AuthenticationPrincipal User user, RedirectAttributes model) {
        try {
            teamService.leftTeam(teamId, user.getId());
        } catch (Exception e) {
            model.addFlashAttribute("error", "smth really wrong: " + e.getMessage());
        }
        return "redirect:/team/view/" + teamId.toString();
    }

    @GetMapping("/edit/{teamId}")
    public String editTeam(@PathVariable UUID teamId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserCapOfTeam(user.getId(), teamId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        try {
            Team team = teamService.getById(teamId);
            model.addAttribute("team", TeamDTO.builder()
                    .name(team.getName())
                    .sport(team.getSport())
                    .logo(team.getLogo())
                    .build());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Команда не найдена");
        }
        return "team/edit_team";
    }

    @PostMapping("/edit/{teamId}")
    public String editTeam(@PathVariable UUID teamId, 
                         @Valid @ModelAttribute("team") TeamDTO team, 
                         BindingResult bindingResult, 
                         Model model,
                         @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        if (bindingResult.hasErrors()) {
            return "team/edit_team";
        }

        try {
            Team existingTeam = teamService.getById(teamId);
            if (logoFile != null && !logoFile.isEmpty()) {
                if (existingTeam.getLogo() != null) {
                    fileStorageService.deleteFile(existingTeam.getLogo());
                }
                String fileName = fileStorageService.storeFile(logoFile);
                team.setLogo(fileName);
            } else {
                team.setLogo(existingTeam.getLogo());
            }
            team.setSport(existingTeam.getSport());
            teamService.editTeam(teamId, team);
            return "redirect:/team/view/" + teamId;
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "team/edit_team";
        }
    }
}
