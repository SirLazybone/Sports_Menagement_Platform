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
    public void updateUserData(UserDataRequest request, StreamObserver<UserDataResponse> responseObserver) {
        try {
            logger.info("Received updateUserData request for user ID: {}", request.getUserId());
            
            // Получаем пользователя из базы данных
            User user = userService.findById(UUID.fromString(request.getUserId()));
            
            // Создаем ответ с данными пользователя
            UserDataResponse response = buildUserDataResponse(user);
            
            // Отправляем ответ клиенту
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
    public void getAvailableTournaments(Empty request, StreamObserver<TournamentsResponse> responseObserver) {
        try {
            logger.info("Received getAvailableTournaments request");
            
            // Получаем список активных турниров
            List<Tournament> tournaments = tournamentService.findAllActive();
            
            // Создаем ответ с данными о турнирах
            TournamentsResponse response = buildTournamentsResponse(tournaments);
            
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
} 