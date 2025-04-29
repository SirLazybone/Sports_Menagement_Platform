package com.course_work.Sports_Menagement_Platform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("error", "Ресурс не найден");
                return "error/404";
            }
            if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("error", "Доступ запрещён");
                return "error/403";
            }
        }
        model.addAttribute("error", "Произошла ошибка");
        return "error/general";
    }
    
}
