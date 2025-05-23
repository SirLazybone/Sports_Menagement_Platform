package com.course_work.Sports_Menagement_Platform.grpc;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.AfterMatchPenaltyService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GoalService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GroupService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Клиент для вызова gRPC-сервиса генерации PDF-документов
 */
@Service
public class PDFServiceClient {
    private final StageService stageService;
    private final GoalService goalService;
    private final AfterMatchPenaltyService afterMatchPenaltyService;
    private final GroupService groupService;
    
    private static final Logger logger = LoggerFactory.getLogger(PDFServiceClient.class);
    
    private final ManagedChannel channel;
    private final PDFServiceGrpc.PDFServiceBlockingStub blockingStub;
    
    public PDFServiceClient(
            @Value("${grpc.python.server.host}") String host,
            @Value("${grpc.python.server.port}") int port,
            StageService stageService,
            GoalService goalService,
            AfterMatchPenaltyService afterMatchPenaltyService,
            GroupService groupService) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = PDFServiceGrpc.newBlockingStub(channel);
        logger.info("PDF Service client initialized with server at {}:{}", host, port);
        this.stageService = stageService;
        this.goalService = goalService;
        this.afterMatchPenaltyService = afterMatchPenaltyService;
        this.groupService = groupService;
    }
    
    /**
     * Создает PDF-документ для матча
     */
    public PDFResponse createMatchPDF(Match match) {
        try {
            MatchPDFRequest request = createMatchPdfRequest(match);
            logger.info("Sending createMatchPDF request for match ID: {}", match.getId());
            PDFResponse response = blockingStub.createMatchPDF(request);
            logger.info("Received PDF for match ID: {}, status: {}", match.getId(), response.getStatus());
            return response;
        } catch (Exception e) {
            logger.error("Error creating match PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать PDF для матча", e);
        }
    }
    
    /**
     * Создает PDF-документ для турнира
     */
    public PDFResponse createTournamentPDF(Tournament tournament) {
        try {
            TournamentPDFRequest request = createTournamentPdfRequest(tournament);
            logger.info("Sending createTournamentPDF request for tournament ID: {}", tournament.getId());
            PDFResponse response = blockingStub.createTournamentPDF(request);
            logger.info("Received PDF for tournament ID: {}, status: {}", tournament.getId(), response.getStatus());
            return response;
        } catch (Exception e) {
            logger.error("Error creating tournament PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать PDF для турнира", e);
        }
    }
    
    /**
     * Создает запрос на создание PDF-документа для матча
     */
    private MatchPDFRequest createMatchPdfRequest(Match match) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Создаем построитель запроса с основными данными матча
        MatchPDFRequest.Builder requestBuilder = MatchPDFRequest.newBuilder()
                .setMatchId(match.getId().toString())
                .setStageId(match.getStage().getId().toString())
                .setTournamentId(match.getStage().getTournament().getId().toString())
                .setSport(match.getStage().getTournament().getSport().toString())
                .setIsFinished(match.isResultPublished());
        
        // Добавляем информацию о дате матча (из слота)
        if (match.getSlot() != null && match.getSlot().getDate() != null) {
            requestBuilder.setDate(match.getSlot().getDate().format(dateFormatter));
        }
        
        // Добавляем информацию о командах
        if (match.getTeam1() != null) {
            TeamInfo team1Info = TeamInfo.newBuilder()
                    .setTeamId(match.getTeam1().getId().toString())
                    .setName(match.getTeam1().getName())
                    .setSport(match.getTeam1().getSport().toString())
                    .build();
            requestBuilder.addTeams(team1Info);
        }
        
        if (match.getTeam2() != null) {
            TeamInfo team2Info = TeamInfo.newBuilder()
                    .setTeamId(match.getTeam2().getId().toString())
                    .setName(match.getTeam2().getName())
                    .setSport(match.getTeam2().getSport().toString())
                    .build();
            requestBuilder.addTeams(team2Info);
        }
        
        // Добавляем информацию о месте проведения (из слота)
        if (match.getSlot() != null && match.getSlot().getLocation() != null) {
            Location location = match.getSlot().getLocation();
            LocationInfo locationInfo = LocationInfo.newBuilder()
                    .setName(location.getName())
                    .setAddress(location.getAddress())
                    .setCity(match.getStage().getTournament().getCity().getName()) // Берем город из турнира
                    .build();
            requestBuilder.setLocation(locationInfo);
        }
        
        // Получаем голы из сервиса
        List<Goal> goals = goalService.getGoalsByMatch(match.getId());
        
        // Получаем количество голов для каждой команды
        int team1Goals = 0;
        int team2Goals = 0;
        
        if (match.getTeam1() != null && match.getTeam2() != null) {
            team1Goals = goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam1().getId());
            team2Goals = goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam2().getId());
        }
        
        // Добавляем счет матча
        ScoreInfo scoreInfo = ScoreInfo.newBuilder()
                .setTeam1(team1Goals)
                .setTeam2(team2Goals)
                .build();
        requestBuilder.setScore(scoreInfo);
        
        // Добавляем информацию о голах
        for (Goal goal : goals) {
            GoalInfo goalInfo = GoalInfo.newBuilder()
                    .setTeamId(goal.getTeam().getId().toString())
                    .setUserName(goal.getPlayer().getName())
                    .setUserSurname(goal.getPlayer().getSurname())
                    .setSetNumber(goal.getSet_number())
                    .setTime(String.valueOf(goal.getTime()))
                    .setIsPenalty(goal.isPenalty())
                    .build();
            requestBuilder.addGoals(goalInfo);
        }
        
        // Получаем послематчевые пенальти из сервиса
        boolean hasPenalties = afterMatchPenaltyService.hasPenalties(match.getId());
        if (hasPenalties) {
            List<AfterMatchPenalty> penalties = afterMatchPenaltyService.getPenaltiesByMatch(match.getId());
            for (AfterMatchPenalty penalty : penalties) {
                AfterMatchPenaltyInfo penaltyInfo = AfterMatchPenaltyInfo.newBuilder()
                        .setTeamId(penalty.getTeam().getId().toString())
                        .setUserName(penalty.getPlayer().getName())
                        .setUserSurname(penalty.getPlayer().getSurname())
                        .setIsSuccess(penalty.isScored())
                        .build();
                requestBuilder.addAfterMatchPenalties(penaltyInfo);
            }
        }
        
        return requestBuilder.build();
    }
    
    /**
     * Создает запрос на создание PDF-документа для турнира
     */
    private TournamentPDFRequest createTournamentPdfRequest(Tournament tournament) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Создаем построитель запроса с основными данными турнира
        TournamentPDFRequest.Builder requestBuilder = TournamentPDFRequest.newBuilder()
                .setTournamentId(tournament.getId().toString())
                .setName(tournament.getName())
                .setSport(tournament.getSport().toString())
                .setOrganizationName(tournament.getUserOrgCom().getOrgCom().getName())
                .setCity(tournament.getCity().getName())
                .setRegistrationDeadline(tournament.getRegisterDeadline().format(dateFormatter))
                .setIsStopped(tournament.is_stopped());
        
        // Добавляем описание (если есть)
        if (tournament.getDescription() != null) {
            requestBuilder.setDescription(tournament.getDescription());
        }
        
        // Добавляем логотип (если есть)
        if (tournament.getLogo() != null && !tournament.getLogo().isEmpty()) {
            requestBuilder.setLogo(com.google.protobuf.ByteString.copyFromUtf8(tournament.getLogo()));
        }
        
        // Добавляем информацию о командах
        tournament.getTeamTournamentList().forEach(teamTournament -> {
            Team team = teamTournament.getTeam();
            TeamInfo.Builder teamInfoBuilder = TeamInfo.newBuilder()
                    .setTeamId(team.getId().toString())
                    .setName(team.getName())
                    .setSport(team.getSport().toString());
            
            // Если у команды есть логотип, добавляем его
            if (team.getLogo() != null && !team.getLogo().isEmpty()) {
                teamInfoBuilder.setLogo(com.google.protobuf.ByteString.copyFromUtf8(team.getLogo()));
            }
            
            requestBuilder.addTeams(teamInfoBuilder.build());
        });
        
        // Добавляем информацию о стадиях турнира
        tournament.getStages().forEach(stage -> {
            StageInfo.Builder stageInfoBuilder = StageInfo.newBuilder()
                    .setStageId(stage.getId().toString())
                    .setIsPublished(stage.isPublished())
                    .setBestPlace(stage.getBestPlace())
                    .setWorstPlace(stage.getWorstPlace())
                    .setName(stageService.getStageName(stage.getBestPlace(), stage.getWorstPlace())); // Генерируем имя этапа
            
            // Добавляем матчи для каждой стадии
            stage.getMatches().forEach(match -> {
                MatchInfo.Builder matchInfoBuilder = MatchInfo.newBuilder()
                        .setIsFinished(match.isResultPublished());
                
                // Получаем дату из слота (если есть)
                if (match.getSlot() != null && match.getSlot().getDate() != null) {
                    matchInfoBuilder.setDate(match.getSlot().getDate().format(dateFormatter));
                }
                
                // Добавляем информацию о командах в матче
                if (match.getTeam1() != null) {
                    TeamInfo team1Info = TeamInfo.newBuilder()
                            .setTeamId(match.getTeam1().getId().toString())
                            .setName(match.getTeam1().getName())
                            .setSport(match.getTeam1().getSport().toString())
                            .build();
                    matchInfoBuilder.addTeams(team1Info);
                }
                
                if (match.getTeam2() != null) {
                    TeamInfo team2Info = TeamInfo.newBuilder()
                            .setTeamId(match.getTeam2().getId().toString())
                            .setName(match.getTeam2().getName())
                            .setSport(match.getTeam2().getSport().toString())
                            .build();
                    matchInfoBuilder.addTeams(team2Info);
                }
                
                // Добавляем информацию о месте проведения (из слота)
                if (match.getSlot() != null && match.getSlot().getLocation() != null) {
                    Location location = match.getSlot().getLocation();
                    LocationInfo locationInfo = LocationInfo.newBuilder()
                            .setName(location.getName())
                            .setAddress(location.getAddress())
                            .setCity(tournament.getCity().getName()) // Используем город турнира, т.к. у локации нет city
                            .build();
                    matchInfoBuilder.setLocation(locationInfo);
                }
                
                // Добавляем счет матча
                // Хранится не в модели Match, поэтому добавляем пустой счет
                ScoreInfo scoreInfo = ScoreInfo.newBuilder()
                        .setTeam1(0)  // Требуется логика получения счета
                        .setTeam2(0)  // Требуется логика получения счета
                        .build();
                matchInfoBuilder.setScore(scoreInfo);
                
                stageInfoBuilder.addMatches(matchInfoBuilder.build());
            });
            
            requestBuilder.addStages(stageInfoBuilder.build());
        });
        
        // Добавляем информацию о слотах
        tournament.getLocations().forEach(location -> {
            location.getSlots().forEach(slot -> {
                SlotInfo.Builder slotInfoBuilder = SlotInfo.newBuilder()
                        .setSlotId(slot.getId().toString())
                        .setTournamentId(tournament.getId().toString())
                        .setLocationId(location.getId().toString());
                
                // Добавляем дату, если она есть
                if (slot.getDate() != null) {
                    slotInfoBuilder.setDate(slot.getDate().format(dateFormatter));
                }
                
                requestBuilder.addSlots(slotInfoBuilder.build());
            });
        });
        
        // Добавляем информацию о группах
        List<Group> groups = groupService.getGroups(tournament.getId());
        if (groups != null && !groups.isEmpty()) {
            for (Group group : groups) {
                GroupInfo.Builder groupInfoBuilder = GroupInfo.newBuilder()
                        .setGroupId(group.getId().toString())
                        .setName(group.getName())
                        .setCountTeams(group.getTeams().size());
                
                // Добавляем команды в группе
                group.getTeams().forEach(team -> {
                    TeamInfo teamInfo = TeamInfo.newBuilder()
                            .setTeamId(team.getId().toString())
                            .setName(team.getName())
                            .setSport(team.getSport().toString())
                            .build();
                    groupInfoBuilder.addTeams(teamInfo);
                });
                
                requestBuilder.addGroups(groupInfoBuilder.build());
            }
        }
        
        return requestBuilder.build();
    }
    
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
} 