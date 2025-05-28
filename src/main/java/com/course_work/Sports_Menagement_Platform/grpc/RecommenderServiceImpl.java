package com.course_work.Sports_Menagement_Platform.grpc;

import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Реализация gRPC-сервиса RecommenderService
 * Обрабатывает входящие запросы UpdateUserData и GetAvailableTournaments.
 */
@GrpcService
public class RecommenderServiceImpl extends RecommenderServiceGrpc.RecommenderServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(RecommenderServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TournamentService tournamentService;

    /**
     * Обработка запроса на обновление данных пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public void updateUserData(UserDataRequest request, StreamObserver<UserDataResponse> responseObserver) {
        try {
            logger.info("Received updateUserData request for user ID: {}", request.getUserId());
            
            User user = userService.findByIdWithRelations(UUID.fromString(request.getUserId()));
            
            UserDataResponse response = buildUserDataResponse(user);
            System.out.println("!!! User Data Response:");
            printUserDataResponse(response);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Successfully processed updateUserData request for user ID: {}", request.getUserId());
        } catch (Exception e) {
            logger.error("Error processing updateUserData request: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    /**
     * Обработка запроса на получение доступных турниров
     */
    @Override
    @Transactional(readOnly = true)
    public void getAvailableTournaments(Empty request, StreamObserver<TournamentsResponse> responseObserver) {
        try {
            logger.info("Received getAvailableTournaments request");
            
            // Получаем список активных турниров
            List<Tournament> tournaments = tournamentService.findAllActive();
            
            // Создаем ответ с данными о турнирах
            TournamentsResponse response = buildTournamentsResponse(tournaments);
            System.out.println("!!! Tournament Response:");
            printTournamentsResponse(response);
            
            // Отправляем ответ клиенту
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            logger.info("Successfully processed getAvailableTournaments request, returning {} tournaments", tournaments.size());
        } catch (Exception e) {
            logger.error("Error processing getAvailableTournaments request: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    /**
     * Создает объект ответа с данными пользователя
     */
    private UserDataResponse buildUserDataResponse(User user) {
        // Создаем построитель UserData
        UserData.Builder userDataBuilder = UserData.newBuilder()
                .setName(user.getName())
                .setSurname(user.getSurname());
        
        // Добавляем данные об оргкомитетах пользователя
        user.getUserOrgComList().forEach(userOrgCom -> {
            OrgInfo orgInfo = OrgInfo.newBuilder()
                    .setName(userOrgCom.getOrgCom().getName())
                    .setRole(userOrgCom.getOrgRole().toString())
                    .setIsRef(userOrgCom.isRef())
                    .build();
            userDataBuilder.addOrgInfo(orgInfo);
        });
        
        // Добавляем данные о командах пользователя
        user.getUserTeamList().forEach(userTeam -> {
            TeamInfo teamInfo = TeamInfo.newBuilder()
                    .setTeamId(userTeam.getTeam().getId().toString())
                    .setName(userTeam.getTeam().getName())
                    .setSport(userTeam.getTeam().getSport().toString())
                    .build();
            
            // Если у команды есть логотип, добавляем его
            if (userTeam.getTeam().getLogo() != null && !userTeam.getTeam().getLogo().isEmpty()) {
                teamInfo = teamInfo.toBuilder()
                        .setLogo(com.google.protobuf.ByteString.copyFromUtf8(userTeam.getTeam().getLogo()))
                        .build();
            }
            
            // Если есть статус приглашения, добавляем его
            if (userTeam.getInvitationStatus() != null) {
                teamInfo = teamInfo.toBuilder()
                        .setInvitationStatus(userTeam.getInvitationStatus().toString())
                        .build();
            }
            
            userDataBuilder.addTeams(teamInfo);
        });
        
        // Добавляем данные о турнирах пользователя
        user.getUserTeamList().stream()
                .flatMap(userTeam -> userTeam.getTeam().getTeamTournamentList().stream())
                .forEach(teamTournament -> {
                    Tournament tournament = teamTournament.getTournament();
                    TournamentInfo.Builder tournamentInfoBuilder = TournamentInfo.newBuilder()
                            .setName(tournament.getName())
                            .setOrganizationName(tournament.getUserOrgCom().getOrgCom().getName())
                            .setSport(tournament.getSport().toString())
                            .setCity(tournament.getCity().getName());
                    
                    if (tournament.getDescription() != null) {
                        tournamentInfoBuilder.setDescription(tournament.getDescription());
                    }
                    
                    // Если у турнира есть логотип, добавляем его
                    if (tournament.getLogo() != null && !tournament.getLogo().isEmpty()) {
                        tournamentInfoBuilder.setLogo(com.google.protobuf.ByteString.copyFromUtf8(tournament.getLogo()));
                    }
                    
                    userDataBuilder.addTournaments(tournamentInfoBuilder.build());
                });
        
        // Создаем и возвращаем ответ
        return UserDataResponse.newBuilder()
                .setUserId(user.getId().toString())
                .setUserData(userDataBuilder.build())
                .build();
    }

    /**
     * Создает объект ответа со списком доступных турниров
     */
    private TournamentsResponse buildTournamentsResponse(List<Tournament> tournaments) {
        TournamentsResponse.Builder responseBuilder = TournamentsResponse.newBuilder();
        
        // Добавляем данные о каждом турнире
        tournaments.forEach(tournament -> {
            TournamentInfo.Builder tournamentInfoBuilder = TournamentInfo.newBuilder()
                    .setId(tournament.getId().toString())
                    .setName(tournament.getName())
                    .setOrganizationName(tournament.getUserOrgCom().getOrgCom().getName())
                    .setSport(tournament.getSport().toString())
                    .setCity(tournament.getCity().getName());
            
            if (tournament.getDescription() != null) {
                tournamentInfoBuilder.setDescription(tournament.getDescription());
            }
            
            // Если у турнира есть логотип, добавляем его
            if (tournament.getLogo() != null && !tournament.getLogo().isEmpty()) {
                tournamentInfoBuilder.setLogo(com.google.protobuf.ByteString.copyFromUtf8(tournament.getLogo()));
            }
            
            responseBuilder.addTournaments(tournamentInfoBuilder.build());
        });
        
        return responseBuilder.build();
    }

    /**
     * Выводит данные пользователя в читаемом формате
     */
    private void printUserDataResponse(UserDataResponse response) {
        System.out.println("User ID: " + response.getUserId());
        UserData userData = response.getUserData();
        System.out.println("Name: " + userData.getName());
        System.out.println("Surname: " + userData.getSurname());
        
        System.out.println("Organizations (" + userData.getOrgInfoCount() + "):");
        for (OrgInfo orgInfo : userData.getOrgInfoList()) {
            System.out.println("  - " + orgInfo.getName() + " (Role: " + orgInfo.getRole() + ", Ref: " + orgInfo.getIsRef() + ")");
        }
        
        System.out.println("Teams (" + userData.getTeamsCount() + "):");
        for (TeamInfo teamInfo : userData.getTeamsList()) {
            System.out.println("  - " + teamInfo.getName() + " (Sport: " + teamInfo.getSport() + ")");
        }
        
        System.out.println("Tournaments (" + userData.getTournamentsCount() + "):");
        for (TournamentInfo tournamentInfo : userData.getTournamentsList()) {
            System.out.println("  - " + tournamentInfo.getName() + " (Sport: " + tournamentInfo.getSport() + ", City: " + tournamentInfo.getCity() + ")");
        }
    }
    
    /**
     * Выводит данные турниров в читаемом формате
     */
    private void printTournamentsResponse(TournamentsResponse response) {
        System.out.println("Available Tournaments (" + response.getTournamentsCount() + "):");
        for (TournamentInfo tournament : response.getTournamentsList()) {
            System.out.println("    id: " + tournament.getId());
            System.out.println("  - " + tournament.getName());
            System.out.println("    Sport: " + tournament.getSport());
            System.out.println("    City: " + tournament.getCity());
            System.out.println("    Organization: " + tournament.getOrganizationName());
            if (!tournament.getDescription().isEmpty()) {
                System.out.println("    Description: " + tournament.getDescription());
            }
            System.out.println();
        }
    }
} 