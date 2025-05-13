package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.grpc.*;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.dto.UserCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.TeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.LocationCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;
import com.course_work.Sports_Menagement_Platform.dto.AdditionalMatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.GoalDTO;
import com.course_work.Sports_Menagement_Platform.dto.AfterMatchPenaltyDTO;
import com.course_work.Sports_Menagement_Platform.dto.UserDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.Map;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;
import com.course_work.Sports_Menagement_Platform.dto.InvitationTeamDTO;
import com.course_work.Sports_Menagement_Platform.dto.ProlongRegDTO;
import com.course_work.Sports_Menagement_Platform.dto.TournamentSearchDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GoalService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.AfterMatchPenaltyService;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    private final SportsServiceImpl sportsService;
    private final UserService userService;
    private final TeamService teamService;
    private final OrgComService orgComService;
    private final MatchService matchService;
    private final StageService stageService;
    private final SlotService slotService;
    private final LocationService locationService;
    private final TournamentService tournamentService;
    private final PasswordEncoder passwordEncoder;
    private final AccessService accessService;
    private final GoalService goalService;
    private final AfterMatchPenaltyService afterMatchPenaltyService;

    @Autowired
    public ApiController(SportsServiceImpl sportsService,
                        UserService userService,
                        TeamService teamService,
                        OrgComService orgComService,
                        MatchService matchService,
                        StageService stageService,
                        SlotService slotService,
                        LocationService locationService,
                        TournamentService tournamentService,
                        PasswordEncoder passwordEncoder,
                        AccessService accessService,
                        GoalService goalService,
                        AfterMatchPenaltyService afterMatchPenaltyService) {
        this.sportsService = sportsService;
        this.userService = userService;
        this.teamService = teamService;
        this.orgComService = orgComService;
        this.matchService = matchService;
        this.stageService = stageService;
        this.slotService = slotService;
        this.locationService = locationService;
        this.tournamentService = tournamentService;
        this.passwordEncoder = passwordEncoder;
        this.accessService = accessService;
        this.goalService = goalService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @AuthenticationPrincipal User user) {
        
        // Create recommendation request with user ID
        RecommendationRequest request = RecommendationRequest.newBuilder()
            .setUserId(user.getId().toString())
            .build();
        
        // Get recommendations through gRPC
        RecommendationResponse response = sportsService.getRecommendations(request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tournament/{tournamentId}/visualization")
    public ResponseEntity<byte[]> getTournamentVisualization(@PathVariable String tournamentId) {
        TournamentVisualizationResponse response = sportsService.getTournamentVisualization(tournamentId);
        
        MediaType mediaType = MediaType.parseMediaType("image/" + response.getImageFormat());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(response.getImageData().toByteArray());
    }

    @GetMapping("/match/{matchId}/visualization")
    public ResponseEntity<byte[]> getMatchVisualization(@PathVariable String matchId) {
        MatchVisualizationResponse response = sportsService.getMatchVisualization(matchId);
        
        MediaType mediaType = MediaType.parseMediaType("image/" + response.getImageFormat());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(response.getImageData().toByteArray());
    }

    // --- AUTH ---
    @GetMapping("/auth/login")
    public ResponseEntity<?> loginForm() {
        return ResponseEntity.ok(new UserDTO());
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String tel = loginData.get("tel");
        String password = loginData.get("password");
        if (tel == null || password == null) {
            return ResponseEntity.badRequest().body("Телефон и пароль обязательны");
        }
        try {
            var user = userService.findByTel(tel);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401).body("Неправильный логин или пароль");
            }
            UserDTO userDTO = userService.getDTOUser(tel);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Неправильный логин или пароль");
        }
    }

    @GetMapping("/auth/registration")
    public ResponseEntity<?> registrationForm() {
        return ResponseEntity.ok(new UserCreationDTO());
    }

    @PostMapping("/auth/registration")
    public ResponseEntity<?> register(@RequestBody UserCreationDTO user) {
        try {
            userService.saveNewUser(user);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- TEAM ---
    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeam(@PathVariable String teamId) {
        try {
            var team = teamService.getById(UUID.fromString(teamId));
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Team not found");
        }
    }

    @PostMapping("/team")
    public ResponseEntity<?> createTeam(@RequestBody TeamDTO teamDTO, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        if (teamDTO.getName() == null || teamDTO.getName().isEmpty() || teamDTO.getSport() == null) {
            return ResponseEntity.badRequest().body("Не выбран спорт или название");
        }
        try {
            var team = teamService.createTeam(teamDTO, user);
            return ResponseEntity.status(201).body(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/team/show_all")
    public ResponseEntity<?> showAllTeams(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            var teams = teamService.getAllActiveTeamByUser(user);
            return ResponseEntity.ok(teams);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/team/new")
    public ResponseEntity<?> newTeamForm() {
        return ResponseEntity.ok(new TeamDTO());
    }

    @GetMapping("/team/create_invitation/{teamId}")
    public ResponseEntity<?> createTeamInvitationForm(@PathVariable String teamId) {
        return ResponseEntity.ok(new InvitationTeamDTO());
    }

    @PostMapping("/team/send_invitation/{teamId}")
    public ResponseEntity<?> sendTeamInvitation(@PathVariable String teamId, @RequestBody Map<String, String> data) {
        return ResponseEntity.ok(new InvitationTeamDTO());
    }

    @PostMapping("/team/kick/{teamId}/{userTel}")
    public ResponseEntity<?> kickFromTeam(@PathVariable String teamId, @PathVariable String userTel, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!teamService.isCap(UUID.fromString(teamId), user.getId())) {
                return ResponseEntity.status(403).body("Пользователь не капитан (нет доступа)");
            }
            var kicked = userService.findByTel(userTel);
            teamService.kickUser(UUID.fromString(teamId), kicked.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userTeam не найден");
            if (e.getMessage().contains("ACCEPTED")) return ResponseEntity.status(409).body("userTeam не в статусе ACCEPTED");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/team/cancel/{teamId}/{userTel}")
    public ResponseEntity<?> cancelTeamInvitation(@PathVariable String teamId, @PathVariable String userTel, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!teamService.isCap(UUID.fromString(teamId), user.getId())) {
                return ResponseEntity.status(403).body("Пользователь не капитан (нет доступа)");
            }
            var canceled = userService.findByTel(userTel);
            teamService.cancelInvitation(UUID.fromString(teamId), canceled.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userTeam не найден");
            if (e.getMessage().contains("PENDING")) return ResponseEntity.status(409).body("userTeam не в статусе PENDING");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/team/leave/{teamId}")
    public ResponseEntity<?> leaveTeam(@PathVariable String teamId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            teamService.leftTeam(UUID.fromString(teamId), user.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userTeam не найден");
            if (e.getMessage().contains("ACCEPTED")) return ResponseEntity.status(409).body("Пользователь – единственный капитан");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/team/edit/{teamId}")
    public ResponseEntity<?> editTeamForm(@PathVariable String teamId) {
        return ResponseEntity.ok(new TeamDTO());
    }

    @PostMapping("/team/edit/{teamId}")
    public ResponseEntity<?> editTeam(@PathVariable String teamId, @RequestBody TeamDTO teamDTO, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!teamService.isCap(UUID.fromString(teamId), user.getId())) {
                return ResponseEntity.status(403).body("Нет доступа (пользователь – не капитан)");
            }
            teamService.editTeam(UUID.fromString(teamId), teamDTO);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No such team")) return ResponseEntity.status(404).body("Команда не найдена");
            if (e.getMessage().contains("уже занято")) return ResponseEntity.badRequest().body("Название пустое");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- ORG_COM ---
    @GetMapping("/org_com/{orgComId}")
    public ResponseEntity<?> getOrgCom(@PathVariable String orgComId) {
        try {
            var orgCom = orgComService.getById(UUID.fromString(orgComId));
            return ResponseEntity.ok(orgCom);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("OrgCom not found");
        }
    }

    @GetMapping("/org_com/new")
    public ResponseEntity<?> newOrgComForm() {
        return ResponseEntity.ok(new OrgComDTO());
    }

    @PostMapping("/org_com/create")
    public ResponseEntity<?> createOrgCom(@RequestBody OrgComDTO orgComDTO, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        if (orgComDTO.getName() == null || orgComDTO.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Название пустое");
        }
        try {
            var orgCom = orgComService.create(orgComDTO.getName(), user);
            return ResponseEntity.status(201).body(orgCom);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/org_com/create_invitation/{orgComId}")
    public ResponseEntity<?> createOrgComInvitation(@PathVariable String orgComId, @RequestBody Map<String, Object> data, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        String tel = (String) data.get("tel");
        String orgRoleStr = (String) data.getOrDefault("orgRole", "ORG");
        boolean isRef = (boolean) data.getOrDefault("isRef", false);
        Org orgRole = Org.valueOf(orgRoleStr);
        if (!orgComService.isUserOfOrgComChief(user.getId(), UUID.fromString(orgComId))) {
            return ResponseEntity.status(403).body("Нет доступа – пользователь не старший организатор");
        }
        if (tel == null || tel.isEmpty()) return ResponseEntity.badRequest().body("Пользователь не найден");
        try {
            var invited = userService.findByTel(tel);
            var orgCom = orgComService.getById(UUID.fromString(orgComId));
            orgComService.createInvitation(orgCom, invited, orgRole, isRef);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("Пользователь не найден");
            if (e.getMessage().contains("уже есть")) return ResponseEntity.status(409).body("Приглашение на этого пользователья уже есть в статусе PENDING или ACCEPTED");
            if (e.getMessage().contains("Оргкомитет не найден")) return ResponseEntity.status(404).body("Оргкомитет не найден");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/org_com/show_all")
    public ResponseEntity<?> showAllOrgComs(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            var orgComs = orgComService.getAllActiveOrgComByUser(user);
            return ResponseEntity.ok(orgComs);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/org_com/view/{id}")
    public ResponseEntity<?> viewOrgCom(@PathVariable String id, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!orgComService.isUserOfOrgComOrg(user.getId(), UUID.fromString(id))) {
                return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
            }
            var orgCom = orgComService.getById(UUID.fromString(id));
            return ResponseEntity.ok(orgCom);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Оргкомитет не найден");
        }
    }

    @PostMapping("/org_com/left/{id}")
    public ResponseEntity<?> leftOrgCom(@PathVariable String id, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            orgComService.leftOrgCom(UUID.fromString(id), user);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userOrgCom не найден");
            if (e.getMessage().contains("ACCEPTED")) return ResponseEntity.status(409).body("Пользователь – единственный старший организатор");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/org_com/kick/{orgComId}/{userTel}")
    public ResponseEntity<?> kickFromOrgCom(@PathVariable String orgComId, @PathVariable String userTel, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!orgComService.isUserOfOrgComChief(user.getId(), UUID.fromString(orgComId))) {
                return ResponseEntity.status(403).body("Пользователь не старший организатор (нет доступа)");
            }
            var kicked = userService.findByTel(userTel);
            orgComService.kickUser(UUID.fromString(orgComId), kicked.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userOrgCom не найден");
            if (e.getMessage().contains("ACCEPTED")) return ResponseEntity.status(409).body("userOrgCom не в статусе ACCEPTED");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/org_com/cancel/{orgComId}/{userTel}")
    public ResponseEntity<?> cancelOrgComInvitation(@PathVariable String orgComId, @PathVariable String userTel, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!orgComService.isUserOfOrgComChief(user.getId(), UUID.fromString(orgComId))) {
                return ResponseEntity.status(403).body("Пользователь не старший организатор (нет доступа)");
            }
            var canceled = userService.findByTel(userTel);
            orgComService.cancelInvitation(UUID.fromString(orgComId), canceled.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) return ResponseEntity.status(404).body("userOrgCom не найден");
            if (e.getMessage().contains("PENDING")) return ResponseEntity.status(409).body("userOrgCom не в статусе PENDING");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/org_com/edit/{orgComId}")
    public ResponseEntity<?> editOrgComForm(@PathVariable String orgComId) {
        return ResponseEntity.ok(new OrgComDTO());
    }

    @PostMapping("/org_com/edit/{orgComId}")
    public ResponseEntity<?> editOrgCom(@PathVariable String orgComId, @RequestBody OrgComDTO orgComDTO, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!orgComService.isUserOfOrgComChief(user.getId(), UUID.fromString(orgComId))) {
                return ResponseEntity.status(403).body("Нет доступа (пользователь – не старший орг)");
            }
            orgComService.editOrgCom(UUID.fromString(orgComId), orgComDTO);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No such OrgCom")) return ResponseEntity.status(404).body("Оргкомитет на найден");
            if (e.getMessage().contains("существует")) return ResponseEntity.badRequest().body("Название пустое");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/org_com/tournaments/{orgComId}")
    public ResponseEntity<?> orgComTournaments(@PathVariable String orgComId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Пользователь не авторизован");
        try {
            if (!orgComService.isUserOfOrgComOrg(user.getId(), UUID.fromString(orgComId))) {
                return ResponseEntity.status(403).body("Нет доступа  (пользователь – не член оргкомитета)");
            }
            var tournaments = tournamentService.getAllTournamentsOfOrgCom(UUID.fromString(orgComId));
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Оргкомитет не найден");
        }
    }

    // --- MATCH ---
    @GetMapping("/match/{matchId}")
    public ResponseEntity<?> getMatch(@PathVariable String matchId) {
        try {
            var match = matchService.getById(UUID.fromString(matchId));
            return ResponseEntity.ok(match);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Match not found");
        }
    }

    @PostMapping("/match")
    public ResponseEntity<?> createMatch(@RequestBody MatchDTO matchDTO) {
        try {
            matchService.createMatches(java.util.List.of(matchDTO));
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/match/additional/{tournamentId}")
    public ResponseEntity<?> getAdditionalMatches(@PathVariable String tournamentId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        if (!accessService.isUserOrgOfTournament(user.getId(), UUID.fromString(tournamentId))) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
        }
        try {
            var matches = matchService.getAdditionalMatchesInWork(UUID.fromString(tournamentId));
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Чемпионат не найден");
        }
    }

    @PostMapping("/match/new_additional/{tournamentId}")
    public ResponseEntity<?> createAdditionalMatch(@PathVariable String tournamentId, @RequestBody AdditionalMatchDTO dto, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        if (!accessService.isUserOrgOfTournament(user.getId(), UUID.fromString(tournamentId))) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член чемпионата)");
        }
        if (dto.getTeam1() == null || dto.getTeam2() == null || dto.getSlot() == null) {
            return ResponseEntity.badRequest().body("Не выбрана одна из команд или слот");
        }
        if (dto.getTeam1().equals(dto.getTeam2())) {
            return ResponseEntity.badRequest().body("С обеих сторон одна команда (team1 = team2)");
        }
        try {
            matchService.createAdditionalMatch(UUID.fromString(tournamentId), dto);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/match/fill_group_stage/{stageId}")
    public ResponseEntity<?> getFillGroupStage(@PathVariable String stageId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        var stage = stageService.getStageById(UUID.fromString(stageId));
        if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
        }
        if (!stageService.isGroupStage(UUID.fromString(stageId))) {
            return ResponseEntity.badRequest().body("Выбран не групповой этап");
        }
        if (stage.isPublished()) {
            return ResponseEntity.badRequest().body("Этап уже опубликован");
        }
        // Можно вернуть структуру для фронта (например, список команд, групп и т.д.)
        return ResponseEntity.ok(stage);
    }

    @PostMapping("/match/fill_group_stage/{stageId}")
    public ResponseEntity<?> fillGroupStage(@PathVariable String stageId, @RequestBody Map<String, String> slotAssignments, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        var stage = stageService.getStageById(UUID.fromString(stageId));
        if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
        }
        if (!stageService.isGroupStage(UUID.fromString(stageId))) {
            return ResponseEntity.badRequest().body("Выбран не групповой этап");
        }
        if (stage.isPublished()) {
            return ResponseEntity.badRequest().body("Этап уже опубликован");
        }
        // TODO: Проверить корректность распределения команд по группам, занятые слоты и т.д.
        try {
            // Преобразовать slotAssignments в нужный формат и вызвать matchService.setSlots(...)
            // matchService.setSlots(UUID.fromString(stageId), assignments);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/match/fill_playoff_stage/{stageId}")
    public ResponseEntity<?> getFillPlayoffStage(@PathVariable String stageId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        var stage = stageService.getStageById(UUID.fromString(stageId));
        if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
        }
        if (stageService.isGroupStage(UUID.fromString(stageId))) {
            return ResponseEntity.badRequest().body("Выбран не этап плей-оффа");
        }
        if (stage.isPublished()) {
            return ResponseEntity.badRequest().body("Этап уже опубликован");
        }
        return ResponseEntity.ok(stage);
    }

    @PostMapping("/match/fill_playoff_stage/{stageId}")
    public ResponseEntity<?> fillPlayoffStage(@PathVariable String stageId, @RequestBody Map<String, String> formData, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        var stage = stageService.getStageById(UUID.fromString(stageId));
        if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не член оргкомитета)");
        }
        if (stageService.isGroupStage(UUID.fromString(stageId))) {
            return ResponseEntity.badRequest().body("Выбран не этап плей-оффа");
        }
        if (stage.isPublished()) {
            return ResponseEntity.badRequest().body("Этап уже опубликован");
        }
        // TODO: Проверить корректность распределения команд, занятые слоты и т.д.
        try {
            // Преобразовать formData в нужный формат и вызвать matchService.setSlotsForPlayOff(...)
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/match/update_slot")
    public ResponseEntity<?> updateMatchSlot(@RequestParam String matchId, @RequestParam String slotId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        var match = matchService.getById(UUID.fromString(matchId));
        var stage = match.getStage();
        if (!accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId())) {
            return ResponseEntity.status(403).body("Нет доступа");
        }
        // TODO: Проверить, опубликованы ли результаты матча
        try {
            matchService.assignSlotToMatch(UUID.fromString(slotId), UUID.fromString(matchId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/match/view/{matchId}")
    public ResponseEntity<?> viewMatch(@PathVariable String matchId, @AuthenticationPrincipal User user) {
        var match = matchService.getById(UUID.fromString(matchId));
        var stage = match.getStage();
        if (!stage.isPublished() && (user == null || !accessService.isUserOrgOfTournament(user.getId(), stage.getTournament().getId()))) {
            return ResponseEntity.status(403).body("Нет доступа (этап матча еще не опубликован, если смотрит не член оргкомитета)");
        }
        return ResponseEntity.ok(match);
    }

    @PostMapping("/match/add_goal/{matchId}")
    public ResponseEntity<?> addGoal(@PathVariable String matchId, @RequestBody GoalDTO goalDTO, @AuthenticationPrincipal User user) {
        
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            Match match = matchService.getById(UUID.fromString(matchId));
            accessService.isUserRefOfTournament(user.getId(), match.getStage().getTournament().getId());
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не судья)");
        }
        try {
            goalService.addGoal(goalDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/match/add_after_match_penalties/{matchId}")
    public ResponseEntity<?> addAfterMatchPenalties(@PathVariable String matchId, @RequestBody AfterMatchPenaltyDTO penalties, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            Match match = matchService.getById(UUID.fromString(matchId));
            accessService.isUserRefOfTournament(user.getId(), match.getStage().getTournament().getId());
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не судья)");
        }
        try {
            afterMatchPenaltyService.addPenalties(penalties);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/match/withdraw/{matchId}")
    public ResponseEntity<?> withdrawFromMatch(@PathVariable String matchId, @RequestParam String teamId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            accessService.isUserCapOfTeam(user.getId(), UUID.fromString(teamId));
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Нет доступа (пользователь – не судья)");
        }
        try {
            // Реализовать снятие команды с матча
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- STAGE ---
    @GetMapping("/stage/{stageId}")
    public ResponseEntity<?> getStage(@PathVariable String stageId) {
        try {
            var stage = stageService.getStageById(UUID.fromString(stageId));
            return ResponseEntity.ok(stage);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Stage not found");
        }
    }

    @PostMapping("/stage/{tournamentId}")
    public ResponseEntity<?> createStage(@RequestBody StageCreationDTO stageDTO, @PathVariable String tournamentId) {
        try {
            stageService.createStage(stageDTO, UUID.fromString(tournamentId));
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stage/publish_stage/{stageId}")
    public ResponseEntity<?> publishStage(@PathVariable String stageId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            var stage = stageService.getStageById(UUID.fromString(stageId));
            var tournamentId = stage.getTournament().getId();
            if (!tournamentService.isUserChiefOfTournament(user.getId(), tournamentId)) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может публиковать этап)");
            }
            stageService.publishStage(UUID.fromString(stageId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/stage/manage_groups/{tournamentId}")
    public ResponseEntity<?> manageGroups(@PathVariable String tournamentId, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), UUID.fromString(tournamentId))) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может управлять группами)");
            }
            var stages = stageService.getStagesByTournament(UUID.fromString(tournamentId));
            // Можно вернуть список групп по этапам
            return ResponseEntity.ok(stages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stage/create_groups/{tournamentId}")
    public ResponseEntity<?> createGroups(@PathVariable String tournamentId, @RequestBody GroupsDTO groupsDTO, @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Нет доступа (не авторизован)");
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), UUID.fromString(tournamentId))) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может создавать группы)");
            }
            stageService.createGroups(groupsDTO);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- SLOT ---
    @GetMapping("/slot/{slotId}")
    public ResponseEntity<?> getSlot(@PathVariable String slotId) {
        try {
            var slot = slotService.getById(UUID.fromString(slotId));
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Slot not found");
        }
    }

    @PostMapping("/slot")
    public ResponseEntity<?> createSlot(@RequestBody SlotCreationDTO slotDTO) {
        try {
            slotService.createSlot(slotDTO);
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/slot/all/{tournamentId}")
    public ResponseEntity<?> allSlots(@PathVariable String tournamentId) {
        try {
            var slots = slotService.getAllByTournament(UUID.fromString(tournamentId));
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Slots not found");
        }
    }

    @DeleteMapping("/slot/delete/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable String slotId) {
        try {
            slotService.deleteSlot(UUID.fromString(slotId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Slot not found");
        }
    }

    // --- LOCATION ---
    @GetMapping("/location/{locationId}")
    public ResponseEntity<?> getLocation(@PathVariable String locationId) {
        try {
            var location = locationService.getById(UUID.fromString(locationId));
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Location not found");
        }
    }

    @GetMapping("/location/all/{tournamentId}")
    public ResponseEntity<?> getAllLocations(@PathVariable String tournamentId) {
        try {
            var locations = locationService.getLocationsByTournamentId(UUID.fromString(tournamentId));
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Locations not found");
        }
    }

    @PostMapping("/location/create/{tournamentId}")
    public ResponseEntity<?> createLocation(@PathVariable String tournamentId, @RequestBody LocationCreationDTO locationDTO) {
        try {
            var location = locationService.createLocation(locationDTO, UUID.fromString(tournamentId));
            return ResponseEntity.status(201).body(location);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // --- TOURNAMENT ---
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> getTournament(@PathVariable String tournamentId) {
        try {
            var tournament = tournamentService.getById(UUID.fromString(tournamentId));
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Tournament not found");
        }
    }

    @GetMapping("/tournament/all")
    public ResponseEntity<?> getAllTournaments() {
        try {
            var tournaments = tournamentService.getAllTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Tournaments not found");
        }
    }

    @PostMapping("/tournament")
    public ResponseEntity<?> createTournament(@RequestBody TournamentDTO tournamentDTO, @RequestParam("orgComId") UUID orgComId, @AuthenticationPrincipal User user) {
        try {
            var tournament = tournamentService.createTournament(tournamentDTO, user, orgComId);
            return ResponseEntity.status(201).body(tournament);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/tournament/{tournamentId}")
    public ResponseEntity<?> updateTournament(@PathVariable String tournamentId, @RequestBody TournamentDTO tournamentDTO, @AuthenticationPrincipal User user) {
        try {
            // Проверка доступа: только руководитель турнира
            if (!tournamentService.isUserChiefOfTournament(user.getId(), UUID.fromString(tournamentId))) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может редактировать)");
            }
            tournamentService.updateTournament(UUID.fromString(tournamentId), tournamentDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tournament/prolong_register/{tournamentId}")
    public ResponseEntity<?> prolongRegister(@PathVariable String tournamentId, @RequestBody ProlongRegDTO prolongRegDTO, @AuthenticationPrincipal User user) {
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), UUID.fromString(tournamentId))) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может продлить регистрацию)");
            }
            tournamentService.prolongRegister(UUID.fromString(tournamentId), prolongRegDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tournament/cancel/{tournamentId}")
    public ResponseEntity<?> cancelTournament(@PathVariable String tournamentId, @AuthenticationPrincipal User user) {
        try {
            if (!tournamentService.isUserChiefOfTournament(user.getId(), UUID.fromString(tournamentId))) {
                return ResponseEntity.status(403).body("Нет доступа (только руководитель турнира может продлить регистрацию)");
            }
            tournamentService.cancelTournament(UUID.fromString(tournamentId));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/tournament/search")
    public ResponseEntity<?> searchTournaments(@RequestBody TournamentSearchDTO searchDTO) {
        try {
            var tournaments = tournamentService.search(searchDTO);
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}