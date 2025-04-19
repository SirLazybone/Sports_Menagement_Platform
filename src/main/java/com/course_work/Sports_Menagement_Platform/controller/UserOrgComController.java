package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.Org;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserOrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/user_org_com")
public class UserOrgComController {
    private final UserOrgComService userOrgComService;
    private final UserService userService;

    private final OrgComService orgComService;

    public UserOrgComController(UserOrgComService userOrgComService, UserService userService, OrgComService orgComService) {
        this.userOrgComService = userOrgComService;
        this.userService = userService;
        this.orgComService = orgComService;
    }

    @GetMapping("/edit/{orgComId}/{userId}")
    @Transactional(readOnly = true)
    public String showEditForm(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model){
        User userToEdit = userService.findById(userId);
        model.addAttribute("user", userToEdit);
        UserOrgCom userOrgCom = userOrgComService.getUserOrgComByUserIdAndOrgComId(userId, orgComId);
        model.addAttribute("userOrgCom", userOrgCom);
        return "org_com/edit_member";


    }

    @GetMapping("/assign_to_chief/{orgComId}/{userId}")
    public String assignToChief(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model) {
        userOrgComService.setRole(userId, orgComId, Org.CHIEF);
        return "/org_com/view/" + orgComId.toString();
    }
    @GetMapping("/assign_to_org/{orgComId}/{userId}")
    public String assignToOrg(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model) {
        userOrgComService.setRole(userId, orgComId, Org.ORG);
        return "/org_com/view/" + orgComId.toString();
    }
    @GetMapping("/assign_to_none/{orgComId}/{userId}")
    public String assignToNone(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model) {
        userOrgComService.setRole(userId, orgComId, Org.NONE);
        return "/org_com/view/" + orgComId.toString();
    }
    @GetMapping("/assign_to_referee/{orgComId}/{userId}")
    public String assignToReferee(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model) {
        userOrgComService.setRefereeStatus(userId, orgComId, true);
        return "/org_com/view/" + orgComId.toString();
    }



    @GetMapping("/remove_as_referee/{orgComId}/{userId}")
    public String removeAsReferee(@PathVariable UUID orgComId, @PathVariable UUID userId, @AuthenticationPrincipal User user, Model model) {
        userOrgComService.setRefereeStatus(userId, orgComId, false);
        return "/org_com/view/" + orgComId.toString();
    }



}
