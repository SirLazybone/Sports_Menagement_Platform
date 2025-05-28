package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.grpc.RecommendationResponse;
import com.course_work.Sports_Menagement_Platform.grpc.RecommenderServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private RecommenderServiceClient recommenderServiceClient;

    @GetMapping("/home")
    public String home(Model model) {
        // Получаем текущего авторизованного пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            
            // Получаем рекомендации для пользователя (до 5 рекомендаций)
            RecommendationResponse recommendations = recommenderServiceClient.getRecommendations(
                    user.getId().toString(), 6);
            System.out.println("\n\n\nRECOMMENDATIONS!!! : \n" + recommendations.getRecommendationsList().toString() + "\n\n\n");
            // Добавляем рекомендации в модель
            model.addAttribute("recommendations", recommendations.getRecommendationsList());
            model.addAttribute("hasRecommendations", !recommendations.getRecommendationsList().isEmpty());
        }
        
        return "home";
    }

    @GetMapping("/")
    public String base() {
        return "redirect:/home";
    }
}
