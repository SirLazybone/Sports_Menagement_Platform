package com.course_work.Sports_Menagement_Platform.grpc;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.data.enums.*;
import com.course_work.Sports_Menagement_Platform.service.impl.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@GrpcService
public class SportsServiceImpl extends SportsServiceGrpc.SportsServiceImplBase {

    @Autowired
    private UserServiceImpl userService;
    
    @Autowired
    private MatchServiceImpl matchService;
    
    @Autowired
    private TournamentServiceImpl tournamentService;
    
    @Autowired
    private UserTeamServiceImpl userTeamService;
    
    @Autowired
    private UserOrgComImpl userOrgComService;
    
    @Autowired
    private GoalServiceImpl goalService;
    
    @Autowired
    private AfterMatchPenaltyServiceImpl penaltyService;
    
    @Autowired
    private LocationServiceImpl locationService;
    
    @Autowired
    private StageServiceImpl stageService;
    
    @Autowired
    private GroupServiceImpl groupService;
    
    @Autowired
    private SlotServiceImpl slotService;

    @Autowired
    private AfterMatchPenaltyServiceImpl afterMatchPenaltyService;

    @Value("${python.backend.url}")
    private String pythonBackendUrl;



    // Синхронный метод для использования в API контроллере
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        final RecommendationResponse[] response = new RecommendationResponse[1];
        final CountDownLatch latch = new CountDownLatch(1);

        getRecommendations(request, new StreamObserver<RecommendationResponse>() {
            @Override
            public void onNext(RecommendationResponse value) {
                response[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
                throw new RuntimeException("Failed to get recommendations", t);
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        try {
            latch.await(5, TimeUnit.SECONDS);
            return response[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for recommendations", e);
        }
    }

    // Асинхронный метод gRPC
    @Override
    public void getRecommendations(RecommendationRequest request, StreamObserver<RecommendationResponse> responseObserver) {
        try {
            // Get user data from database
            User user = userService.findById(UUID.fromString(request.getUserId()));
            // Collect organization info
            List<OrganizationInfo> orgInfos = userOrgComService.findAllByUserId(user.getId()).stream()
                    .map(userOrgCom -> OrganizationInfo.newBuilder()
                            .setName(userOrgCom.getOrgCom().getName())
                            .setRole(userOrgCom.getOrgRole().toString())
                            .setIsRef(userOrgCom.isRef())
                            .build())
                    .collect(Collectors.toList());

            // Collect team info
            List<TeamInfo> teams = userTeamService.findByUserId(user.getId()).stream()
                    .map(userTeam -> TeamInfo.newBuilder()
                            .setName(userTeam.getTeam().getName())
                            .setSport(userTeam.getTeam().getSport().toString())
                            .setIsCap(userTeam.isCap())
                            .setInvitationStatus(userTeam.getInvitationStatus().toString())
                            .build())
                    .collect(Collectors.toList());

            // Collect tournament info
            List<TournamentInfo> tournaments = userTeamService.findByUserId(user.getId()).stream()
                    .flatMap(userTeam -> userTeam.getTeam().getTeamTournamentList().stream())
                    .map(teamTournament -> TournamentInfo.newBuilder()
                            .setName(teamTournament.getTournament().getName())
                            .setTeamName(teamTournament.getTeam().getName())
                            .setSport(teamTournament.getTournament().getSport().toString())
                            .setCity(teamTournament.getTournament().getCity().toString())
                            .build())
                    .collect(Collectors.toList());

            // Build user data response
            UserResponse userData = UserResponse.newBuilder()
                    .setName(user.getName())
                    .setSurname(user.getSurname())
                    .addAllOrgInfo(orgInfos)
                    .addAllTeams(teams)
                    .addAllTournaments(tournaments)
                    .build();

            // Create gRPC channel to Python backend
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pythonBackendUrl)
                    .usePlaintext()
                    .build();

            try {
                // Create stub
                SportsServiceGrpc.SportsServiceBlockingStub stub = SportsServiceGrpc.newBlockingStub(channel);

                // Send request and get response
                RecommendationResponse response = stub.getRecommendations(request);

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } finally {
                channel.shutdown();
            }
            
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription("Failed to get recommendations: " + e.getMessage())
                .asRuntimeException());
        }
    }

    // Синхронный метод для получения визуализации турнира
    public TournamentVisualizationResponse getTournamentVisualization(String tournamentId) {
        final TournamentVisualizationResponse[] response = new TournamentVisualizationResponse[1];
        final CountDownLatch latch = new CountDownLatch(1);

        getTournamentVisualization(
            TournamentVisualizationRequest.newBuilder()
                .setTournamentId(tournamentId)
                .setTournamentData(getTournamentData(tournamentId))
                .build(),
            new StreamObserver<TournamentVisualizationResponse>() {
                @Override
                public void onNext(TournamentVisualizationResponse value) {
                    response[0] = value;
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                    throw new RuntimeException("Failed to get tournament visualization", t);
                }

                @Override
                public void onCompleted() {
                    latch.countDown();
                }
            }
        );

        try {
            latch.await(5, TimeUnit.SECONDS);
            return response[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for tournament visualization", e);
        }
    }

    // Вспомогательный метод для получения данных турнира
    private TournamentResponse getTournamentData(String tournamentId) {
        Tournament tournament = tournamentService.getById(UUID.fromString(tournamentId));

        List<StageProto> stages = stageService.getStagesByTournament(tournament.getId()).stream()
                .map(stage -> StageProto.newBuilder()
                        .setStageId(stage.getId().toString())
                        .setIsPublished(stage.isPublished())
                        .setBestPlace(stage.getBestPlace())
                        .setWorstPlace(stage.getWorstPlace())
                        .addAllMatches(stage.getMatches().stream()
                                .map(match -> MatchResponse.newBuilder()
                                        .setMatchId(match.getId().toString())
                                        .setStageId(match.getStage().getId().toString())
                                        .setTournamentId(match.getStage().getTournament().getId().toString())
                                        .setSport(match.getStage().getTournament().getSport().toString())
                                        .addAllTeams(Stream.of(match.getTeam1(), match.getTeam2())
                                                .map(team -> TeamProto.newBuilder()
                                                        .setTeamId(team.getId().toString())
                                                        .setName(team.getName())
                                                        .setLogo(team.getLogo())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .setScore(ScoreProto.newBuilder()
                                                .setTeam1(goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam1().getId()))
                                                .setTeam2(goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam2().getId()))
                                                .build())
                                        .addAllGoals(goalService.getGoalsByMatch(match.getId()).stream()
                                                .map(goal -> GoalProto.newBuilder()
                                                        .setTeamId(goal.getTeam().getId().toString())
                                                        .setUserId(goal.getPlayer().getId().toString())
                                                        .setSetNumber(goal.getSet_number())
                                                        .setTime(String.valueOf(goal.getTime()))
                                                        .setIsPenalty(goal.isPenalty())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .addAllAfterMatchPenalties(afterMatchPenaltyService.getPenaltiesByMatch(match.getId()).stream()
                                                .map(penalty -> AfterMatchPenaltyProto.newBuilder()
                                                        .setUserId(penalty.getPlayer().getId().toString())
                                                        .setTeamId(penalty.getTeam().getId().toString())
                                                        .setIsSuccess(penalty.isScored())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .setLocation(LocationProto.newBuilder()
                                                .setName(match.getSlot().getLocation().getName())
                                                .setAddress(match.getSlot().getLocation().getAddress())
                                                .setCity(match.getStage().getTournament().getCity().toString())
                                                .build())
                                        .setDate(match.getSlot().getDate().toString())
                                        .setIsFinished(match.isResultPublished())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<TeamInfo> teams = tournament.getTeamTournamentList().stream()
                .map(teamTournament -> TeamInfo.newBuilder()
                        .setName(teamTournament.getTeam().getName())
                        .setLogo(teamTournament.getTeam().getLogo())
                        .setSport(teamTournament.getTeam().getSport().toString())
                        .setIsCap(false)
                        .setInvitationStatus(teamTournament.getApplicationStatus().toString())
                        .build())
                .collect(Collectors.toList());


        List<GroupProto> groups = groupService.getGroups(tournament.getId()).stream()
                .map(group -> GroupProto.newBuilder()
                        .setGroupId(group.getId().toString())
                        .setName(group.getName())
                        .setCountTeams(group.getTeams().size())
                        .addAllTeams(group.getTeams().stream()
                                .map(team -> TeamProto.newBuilder()
                                        .setName(team.getName())
                                        .setLogo(team.getLogo())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return TournamentResponse.newBuilder()
                .setTournamentId(tournament.getId().toString())
                .setName(tournament.getName())
                .setSport(tournament.getSport().toString())
                .setOrganizationName(tournament.getUserOrgCom().getOrgCom().getName())
                .setLogo(tournament.getLogo())
                .setDescription(tournament.getDescription())
                .setCity(tournament.getCity().getName())
                .setRegistrationDeadline(tournament.getRegisterDeadline().toString())
                .setIsStopped(tournament.is_stopped())
                .addAllStages(stages)
                .addAllTeams(teams)
                .addAllGroups(groups)
                .build();
    }

    // Асинхронный метод gRPC для визуализации
    @Override
    public void getTournamentVisualization(TournamentVisualizationRequest request, StreamObserver<TournamentVisualizationResponse> responseObserver) {
        try {
            // Create gRPC channel to Python backend
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pythonBackendUrl)
                    .usePlaintext()
                    .build();

            try {
                // Create stub
                SportsServiceGrpc.SportsServiceBlockingStub stub = SportsServiceGrpc.newBlockingStub(channel);

                // Send request and get response
                TournamentVisualizationResponse response = stub.getTournamentVisualization(request);

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } finally {
                channel.shutdown();
            }
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription("Failed to get tournament visualization: " + e.getMessage())
                .asRuntimeException());
        }
    }

    // Синхронный метод для получения визуализации матча
    public MatchVisualizationResponse getMatchVisualization(String matchId) {
        final MatchVisualizationResponse[] response = new MatchVisualizationResponse[1];
        final CountDownLatch latch = new CountDownLatch(1);

        getMatchVisualization(
            MatchVisualizationRequest.newBuilder()
                .setMatchId(matchId)
                .setMatchData(getMatchData(matchId))
                .build(),
            new StreamObserver<MatchVisualizationResponse>() {
                @Override
                public void onNext(MatchVisualizationResponse value) {
                    response[0] = value;
                }

                @Override
                public void onError(Throwable t) {
                    latch.countDown();
                    throw new RuntimeException("Failed to get match visualization", t);
                }

                @Override
                public void onCompleted() {
                    latch.countDown();
                }
            }
        );

        try {
            latch.await(5, TimeUnit.SECONDS);
            return response[0];
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for match visualization", e);
        }
    }

    // Вспомогательный метод для получения данных матча
    private MatchResponse getMatchData(String matchId) {
        Match match = matchService.getById(UUID.fromString(matchId));

        return MatchResponse.newBuilder()
                .setMatchId(match.getId().toString())
                .setStageId(match.getStage().getId().toString())
                .setTournamentId(match.getStage().getTournament().getId().toString())
                .setSport(match.getStage().getTournament().getSport().toString())
                .addAllTeams(Stream.of(match.getTeam1(), match.getTeam2())
                        .map(team -> TeamProto.newBuilder()
                                .setTeamId(team.getId().toString())
                                .setName(team.getName())
                                .setLogo(team.getLogo())
                                .build())
                        .collect(Collectors.toList()))
                .setScore(ScoreProto.newBuilder()
                        .setTeam1(goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam1().getId()))
                        .setTeam2(goalService.getGoalsByMatchAndTeamCount(match.getId(), match.getTeam2().getId()))
                        .build())
                .addAllGoals(goalService.getGoalsByMatch(match.getId()).stream()
                        .map(goal -> GoalProto.newBuilder()
                                .setTeamId(goal.getTeam().getId().toString())
                                .setUserId(goal.getPlayer().getId().toString())
                                .setSetNumber(goal.getSet_number())
                                .setTime(String.valueOf(goal.getTime()))
                                .setIsPenalty(goal.isPenalty())
                                .build())
                        .collect(Collectors.toList()))
                .addAllAfterMatchPenalties(afterMatchPenaltyService.getPenaltiesByMatch(match.getId()).stream()
                        .map(penalty -> AfterMatchPenaltyProto.newBuilder()
                                .setUserId(penalty.getPlayer().getId().toString())
                                .setTeamId(penalty.getTeam().getId().toString())
                                .setIsSuccess(penalty.isScored())
                                .build())
                        .collect(Collectors.toList()))
                .setLocation(LocationProto.newBuilder()
                        .setName(match.getSlot().getLocation().getName())
                        .setAddress(match.getSlot().getLocation().getAddress())
                        .setCity(match.getStage().getTournament().getCity().toString())
                        .build())
                .setDate(match.getSlot().getDate().toString())
                .setIsFinished(match.isResultPublished())
                .build();
    }

    // Асинхронный метод gRPC для визуализации матча
    @Override
    public void getMatchVisualization(MatchVisualizationRequest request, StreamObserver<MatchVisualizationResponse> responseObserver) {
        try {
            // Create gRPC channel to Python backend
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pythonBackendUrl)
                    .usePlaintext()
                    .build();

            try {
                // Create stub
                SportsServiceGrpc.SportsServiceBlockingStub stub = SportsServiceGrpc.newBlockingStub(channel);

                // Send request and get response
                MatchVisualizationResponse response = stub.getMatchVisualization(request);

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } finally {
                channel.shutdown();
            }
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription("Failed to get match visualization: " + e.getMessage())
                .asRuntimeException());
        }
    }
} 