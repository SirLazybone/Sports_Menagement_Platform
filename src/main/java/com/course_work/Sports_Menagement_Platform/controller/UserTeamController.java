package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.data.models.UserTeam;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserTeamService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

// TODO: добавить проверки

@Controller
@RequestMapping("/edit_user_team")
public class UserTeamController {
    private final UserTeamService userTeamService;
    private final AccessService accessService;

    public UserTeamController(UserTeamService userTeamService, AccessService accessService) {
        this.userTeamService = userTeamService;
        this.accessService = accessService;
    }

    @GetMapping("/{userTeamId}")
    public String getForm(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        try {
            if (!accessService.isUserCap(userTeamId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        UserTeam userTeam = userTeamService.findById(userTeamId);
        model.addAttribute("user_team", userTeam);
        model.addAttribute("userId", user.getId());
        model.addAttribute("isOnlyCap", userTeamService.isOnlyCap(userTeam));
        return "team/edit_member";

    }


    @GetMapping("/remove_as_cap/{userTeamId}")
    public String removeAsCap(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setCapStatus(userTeamId, false);
        return "redirect:edit_user_team/" + userTeamId.toString();
    }


    @GetMapping("/assign_as_cap/{userTeamId}")
    public String assignAsCap(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setCapStatus(userTeamId, true);
        return "redirect:edit_user_team/" + userTeamId.toString();
    }

    @GetMapping("/remove_as_playing/{userTeamId}")
    public String removeAsPlaying(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setPlaying(userTeamId, false);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }

    @GetMapping("/assign_as_playing/{userTeamId}")
    public String assignAsPlaying(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setPlaying(userTeamId, true);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }

    @GetMapping("/cancel_invitation/{userTeamId}")
    public String cancelInvitation(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setInvitationStatis(userTeamId, InvitationStatus.CANCELED);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }

    @GetMapping("/remove/{userTeamId}")
    public String remove(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setInvitationStatis(userTeamId, InvitationStatus.KICKED);

        return "redirect:edit_user_team/" + userTeamId.toString();

    }


    @GetMapping("/leave/{userTeamId}")
    public String leave(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setInvitationStatis(userTeamId, InvitationStatus.LEFT);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }

    @GetMapping("/acceptInvitation/{userTeamId}")
    public String join(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setInvitationStatis(userTeamId, InvitationStatus.ACCEPTED);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }


    @GetMapping("/declineInvitation/{userTeamId}")
    public String decline(@PathVariable UUID userTeamId, Model model, @AuthenticationPrincipal User user) {
        userTeamService.setInvitationStatis(userTeamId, InvitationStatus.DECLINED);
        return "redirect:edit_user_team/" + userTeamId.toString();

    }

}
