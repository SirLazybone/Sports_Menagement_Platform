package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.InvitationStatus;
import com.course_work.Sports_Menagement_Platform.data.models.OrgCom;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.data.models.UserOrgCom;
import com.course_work.Sports_Menagement_Platform.service.interfaces.OrgComService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.UserTeamService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessService {
    private final TournamentService tournamentService;
    private final OrgComService orgComService;
    private final TeamService teamService;
    public AccessService(TournamentService tournamentService, OrgComService orgComService,
                         TeamService teamService) {
        this.tournamentService = tournamentService;
        this.orgComService = orgComService;
        this.teamService = teamService;
    }


    public boolean isUserChiefOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = tournamentService.getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComChief(userId, orgCom.getId());
    }

    public boolean isUserChiefOfOrgCom(UUID userId, UUID orgComId) {
        return orgComService.isUserOfOrgComChief(userId, orgComId);
    }

    public boolean isUserRefOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = tournamentService.getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComRef(userId, orgCom.getId());
    }

    public boolean isUserOrgOfTournament(UUID userId, UUID tournamentId) {
        Tournament tournament = tournamentService.getById(tournamentId);
        OrgCom orgCom = tournament.getUserOrgCom().getOrgCom();
        return orgComService.isUserOfOrgComOrg(userId, orgCom.getId());
    }

    public boolean isUserMemberOfOrgCom(UUID userId, UUID orgComId) {
        UserOrgCom userOrgCom = orgComService.getUserOrgComByUserAndOrgCom(userId, orgComId);
        return userOrgCom.getInvitationStatus().equals(InvitationStatus.ACCEPTED);
    }

    public boolean isUserCapOfTeam(UUID userId, UUID teamId) {
        return teamService.isCap(teamId, userId);
    }

    public boolean isUserCap(UUID userTeamId) {
        return teamService.isCapOfUserTeam(userTeamId);
    }
}
