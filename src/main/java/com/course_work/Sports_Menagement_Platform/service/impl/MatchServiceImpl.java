package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.SlotService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final TeamService teamService;
    private final StageService stageService;
    private final SlotService slotService;
    public MatchServiceImpl(MatchRepository matchRepository, TeamService teamService,
                            StageService stageService, SlotService slotService) {
        this.matchRepository = matchRepository;
        this.teamService = teamService;
        this.stageService = stageService;
        this.slotService = slotService;
    }

    @Override
    public Match getById(UUID matchId) {
        return matchRepository.findById(matchId).orElseThrow(() -> new RuntimeException("Такого матча нет"));
    }

    @Override
    public List<Match> getAllMatches(UUID stageId) {
        return matchRepository.findAllMatchesByStageId(stageId);
    }

    @Override
    public void createMatches(List<MatchDTO> matchDTOs) {
        for (MatchDTO matchDTO : matchDTOs) {
            if (matchDTO.getTeam1Id() == null || matchDTO.getTeam2Id() == null) {
                continue;
            }
            Stage stage = stageService.getStageById(matchDTO.getStageId());
            Team team1 = teamService.getById(matchDTO.getTeam1Id());
            Team team2 = teamService.getById(matchDTO.getTeam2Id());
            boolean team1Exists = matchRepository.existsByStageAndTeam(stage, team1);
            boolean team2Exists = matchRepository.existsByStageAndTeam(stage, team2);

            if (team1Exists || team2Exists) {
                throw new RuntimeException("Одна из команд уже участвует в матче на этом этапе!");
            }

            Match match = Match.builder()
                    .stage(stage)
                    .team1(team1)
                    .team2(team2)
                    .isResultPublished(false)
                    .build();
            try {
                Slot slot = slotService.getById(matchDTO.getSlotId());
                match.setSlot(slot);
            } catch (RuntimeException ignored) {}


            matchRepository.save(match);
        }
    }

    @Override
    public Map<UUID, List<User>> getTeamMembersMap(Team team1, Team team2) {
        Map<UUID, List<User>> players = new HashMap<>();
        players.put(team1.getId(), teamService.getAllUserByTeam(team1.getId()));
        players.put(team2.getId(), teamService.getAllUserByTeam(team2.getId()));
        return players;
    }

    @Override
    public Map<UUID, List<Match>> getMatchesByStagesMap(List<Stage> stages) {
        Map<UUID, List<Match>> matches = new HashMap<>();
        for (Stage stage : stages) {
            matches.put(stage.getId() ,matchRepository.findAllMatchesByStageId(stage.getId()));
        }
        return matches;
    }

    @Override
    public void assignSlotToMatch(UUID slotId, UUID matchId) {
        Match match = getById(matchId);
        Slot slot = slotService.getById(slotId);
        match.setSlot(slot);
        matchRepository.save(match);
    }
}
