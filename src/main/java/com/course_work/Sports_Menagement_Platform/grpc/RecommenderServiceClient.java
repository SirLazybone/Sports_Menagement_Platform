package com.course_work.Sports_Menagement_Platform.grpc;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Клиент для вызова удаленного gRPC сервиса RecommenderService
 */
@Service
public class RecommenderServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommenderServiceClient.class);
    
    @GrpcClient("recommender-service")
    private RecommenderServiceGrpc.RecommenderServiceBlockingStub recommenderServiceStub;
    
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
} 