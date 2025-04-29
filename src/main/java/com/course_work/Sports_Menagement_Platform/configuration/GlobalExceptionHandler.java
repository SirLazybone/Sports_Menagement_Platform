package com.course_work.Sports_Menagement_Platform.configuration;

import com.course_work.Sports_Menagement_Platform.exception.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(Exception ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(Exception ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        return "error/403";
    }
}