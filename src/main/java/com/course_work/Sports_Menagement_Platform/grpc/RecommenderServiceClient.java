package com.course_work.Sports_Menagement_Platform.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Клиент для вызова удаленного gRPC сервиса RecommenderService
 */
@Service
public class RecommenderServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommenderServiceClient.class);
    
    private final ManagedChannel channel;
    private final RecommenderServiceGrpc.RecommenderServiceBlockingStub recommenderServiceStub;
    
    public RecommenderServiceClient(
            @Value("${grpc.python.server.host}") String host,
            @Value("${grpc.python.server.port}") int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.recommenderServiceStub = RecommenderServiceGrpc.newBlockingStub(channel);
        logger.info("Recommender Service client initialized with server at {}:{}", host, port);
    }
    
    /**
     * Получает рекомендации для пользователя
     * 
     * @param userId ID пользователя
     * @param numRecommendations количество рекомендаций
     * @return ответ с рекомендациями
     */
    public RecommendationResponse getRecommendations(String userId, int numRecommendations) {
        return getRecommendations(userId, numRecommendations, Collections.emptyMap());
    }
    
    /**
     * Получает рекомендации для пользователя с дополнительным контекстом
     * 
     * @param userId ID пользователя
     * @param numRecommendations количество рекомендаций
     * @param context дополнительный контекст для рекомендаций
     * @return ответ с рекомендациями
     */
    public RecommendationResponse getRecommendations(String userId, int numRecommendations, Map<String, String> context) {
        try {
            logger.info("Calling remote recommendation service for user ID: {}", userId);
            
            RecommendationRequest.Builder requestBuilder = RecommendationRequest.newBuilder()
                    .setUserId(userId)
                    .setNumRecommendations(numRecommendations);
                    
            // Добавляем контекст, если он есть
            if (context != null && !context.isEmpty()) {
                requestBuilder.putAllContext(context);
            }
            
            RecommendationResponse response = recommenderServiceStub.getRecommendations(requestBuilder.build());
            logger.info("Received {} recommendations for user", response.getRecommendationsCount());
            return response;
        } catch (StatusRuntimeException e) {
            logger.error("Error calling getRecommendations: {}", e.getMessage());
            return RecommendationResponse.newBuilder().build(); // Возвращаем пустой список рекомендаций
        }
    }
    
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
} 