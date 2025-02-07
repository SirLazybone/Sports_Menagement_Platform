package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.OrgComDTO;
import com.course_work.Sports_Menagement_Platform.service.impl.OrgComServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/org_com")
public class OrgComController {
    private final OrgComService orgComService;
    private final UserDetailsService userService;
    public OrgComController(OrgComServiceImpl orgComService, UserDetailsService userService) {
        this.orgComService = orgComService;
        this.userService = userService;
    }

    @GetMapping("/new")
    public String newOrgCom(Model model) {
        model.addAttribute("orgcom", new OrgComDTO());
        return "new_orgcom";
    }


    @PostMapping("/create")
    public String createOrgCom(@Valid @ModelAttribute("orgcom") OrgComDTO orgComDTO, BindingResult bindingResult, Model model, @AuthenticationPrincipal User user) {
        if (bindingResult.hasErrors()) {
            return "new_orgcom";
        }
        try {
            orgComService.create(orgComDTO.getName(), user);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/home";
    }
}
