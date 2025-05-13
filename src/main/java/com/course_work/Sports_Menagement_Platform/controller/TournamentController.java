package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.*;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.FileStorageService;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tournament")
public class TournamentController {
    private final TournamentService tournamentService;
    private final StageService stageService;
    private final MatchService matchService;
    private final CityService cityService;
    private final FileStorageService fileStorageService;

    private final AccessService accessService;

    public TournamentController(TournamentService tournamentService, StageService stageService,
                                MatchService matchService, CityService cityService,
                                FileStorageService fileStorageService, AccessService accessService) {
        this.tournamentService = tournamentService;
        this.stageService = stageService;
        this.matchService = matchService;
        this.cityService = cityService;
        this.fileStorageService = fileStorageService;
        this.accessService = accessService;
    }

    @GetMapping("/search")
    public String search(Model model) {
        List<City> cities = cityService.getCities();
        model.addAttribute("tournamentSearchDTO", new TournamentSearchDTO());
        model.addAttribute("cities", cities);
        model.addAttribute("sportNames", new SportNames().getMap());
        model.addAttribute("tournaments", new ArrayList<>());

        return "tournament/search";
    }

    @PostMapping("/search")
    public String searchPost(Model model, @ModelAttribute("tournamentSearchDTO") TournamentSearchDTO tournamentSearchDTO) {
        List<Tournament> tournaments = tournamentService.search(tournamentSearchDTO);
        model.addAttribute("tournamentSearchDTO", tournamentSearchDTO);
        model.addAttribute("cities", cityService.getCities());
        model.addAttribute("sportNames", new SportNames().getMap());
        model.addAttribute("tournaments", tournaments);

        return "tournament/search";
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
    public String createTournament(Model model, @PathVariable UUID orgComId, @AuthenticationPrincipal User user) {

        try {
            if (!accessService.isUserChiefOfOrgCom(user.getId(), orgComId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("tournament", new TournamentDTO());
        model.addAttribute("cities", cityService.getCities());
        model.addAttribute("orgComId", orgComId);
        return "tournament/create";
    }

    @PostMapping("/create/{orgComId}")
    public String createTournament(@Valid @ModelAttribute("tournament") TournamentDTO tournamentDTO, 
                                 @PathVariable UUID orgComId, 
                                 BindingResult bindingResult, 
                                 Model model, 
                                 @AuthenticationPrincipal User user,
                                 @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        if (bindingResult.hasErrors()) {
            return "tournament/create";
        }

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = fileStorageService.storeFile(logoFile);
                tournamentDTO.setLogo(fileName);
            }
            Tournament tournament = tournamentService.createTournament(tournamentDTO, user, orgComId);
            return "redirect:/org_com/view/" + orgComId.toString();

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/create";
        }
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, @AuthenticationPrincipal User user) {
        try {
            Tournament tournament = tournamentService.getById(id);
            stageService.createGroupStageIfNotExists(id);
            
            List<Stage> stages = tournament.getStages().stream()
                .filter(stage -> stage.getBestPlace() >= 0)
                .collect(Collectors.toList());
            
            // Находим групповой этап
            Stage groupStage = stages.stream()
                .filter(stage -> stage.getBestPlace() == 0 && stage.getWorstPlace() == 0)
                .findFirst()
                .orElse(null);
            
            // Разделяем этапы по статусам
            List<Stage> stagesTeamsKnown = stages.stream()
                .filter(stage -> stageService.getStageStatus(stage) == StageStatus.TEAMS_KNOWN)
                .collect(Collectors.toList());
                
            // Добавляем групповой этап в stagesTeamsKnown, если он существует
            if (groupStage != null && !stagesTeamsKnown.contains(groupStage)) {
                stagesTeamsKnown.add(groupStage);
            }
                
            List<Stage> stagesPublished = stages.stream()
                .filter(stage -> stage.isPublished() && stageService.getStageStatus(stage) != StageStatus.FINISHED)
                .collect(Collectors.toList());
                
            List<Stage> stagesFinished = stages.stream()
                .filter(stage -> stageService.getStageStatus(stage) == StageStatus.FINISHED)
                .collect(Collectors.toList());

            model.addAttribute("tournament", tournament);
            model.addAttribute("stagesTeamsKnown", stagesTeamsKnown);
            model.addAttribute("stagesPublished", stagesPublished);
            model.addAttribute("stagesFinished", stagesFinished);
            
            boolean isUserOrg = false;
            boolean isUserChiefOrg = false;

            if (user != null) {
                List<UserOrgCom> userOrgComList = tournament.getUserOrgCom().getOrgCom().getUserOrgComList().stream()
                    .filter(userOrgCom -> userOrgCom.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
                if (userOrgComList.size() != 0) {
                    isUserOrg = true;
                    if (userOrgComList.get(0).getOrgRole() == Org.CHIEF) isUserChiefOrg = true;
                }
            }

            model.addAttribute("isUserOrg", isUserOrg);
            model.addAttribute("isUserChiefOrg", isUserChiefOrg);
            model.addAttribute("tournamentId", id);
            model.addAttribute("registrationOpen", tournament.getRegisterDeadline().isAfter(ChronoLocalDate.from(LocalDateTime.now())));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw new ResourceNotFoundException("Чемпионат не найден");
        }
        return "tournament/view";
    }

    // TODO: ручка чтобы отменить чемпионат

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

    @GetMapping("/prolong_reg/{tournamentId}")
    public String prolongReg(@PathVariable("tournamentId") UUID tournamentId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        model.addAttribute("prolongReg", new ProlongRegDTO());
        return "/tournament/prolong_reg";
    }

    @PostMapping("/prolong_reg/{tournamentId}")
    public String prolongRegPost(@PathVariable("tournamentId") UUID tournamentId, @ModelAttribute("prolongReg") ProlongRegDTO prolongRegDTO, Model model) {
        try {
            tournamentService.prolongRegister(tournamentId, prolongRegDTO);
        } catch (RuntimeException e) {
            model.addAttribute("error", "Ошибка продления регистрации: " + e.getMessage());
        }
        return "redirect:/tournament/view/" + tournamentId.toString();
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

    @GetMapping("/edit/{tournamentId}")
    public String editTournament(@PathVariable UUID tournamentId, Model model) {
        try {
            Tournament tournament = tournamentService.getById(tournamentId);
            model.addAttribute("tournament", TournamentDTO.builder()
                    .name(tournament.getName())
                    .sport(tournament.getSport())
                    .cityName(tournament.getCity().getName())
                    .minMembers(tournament.getMinMembers())
                    .registerDeadline(tournament.getRegisterDeadline())
                    .description(tournament.getDescription())
                    .logo(tournament.getLogo())
                    .build());
            model.addAttribute("cities", cityService.getCities());
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Чемпионат не найден");
        }
        return "tournament/edit";
    }

    @PostMapping("/edit/{tournamentId}")
    public String editTournament(@PathVariable UUID tournamentId,
                               @Valid @ModelAttribute("tournament") TournamentDTO tournamentDTO,
                               BindingResult bindingResult,
                               Model model,
                               @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {

        try {
            Tournament existingTournament = tournamentService.getById(tournamentId);
            if (logoFile != null && !logoFile.isEmpty()) {
                // Delete old logo if exists
                if (existingTournament.getLogo() != null) {
                    fileStorageService.deleteFile(existingTournament.getLogo());
                }
                // Store new logo
                String fileName = fileStorageService.storeFile(logoFile);
                tournamentDTO.setLogo(fileName);
            } else {
                // Keep existing logo
                tournamentDTO.setLogo(existingTournament.getLogo());
            }
            
            // Only update name and logo, keep other fields from existing tournament
            tournamentDTO.setSport(existingTournament.getSport());
            tournamentDTO.setCityName(existingTournament.getCity().getName());
            tournamentDTO.setMinMembers(existingTournament.getMinMembers());
            tournamentDTO.setRegisterDeadline(existingTournament.getRegisterDeadline());
            tournamentDTO.setDescription(existingTournament.getDescription());
            
            tournamentService.updateTournament(tournamentId, tournamentDTO);

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "tournament/edit";
        }
        return "redirect:/tournament/view/" + tournamentId.toString();
    }
}

