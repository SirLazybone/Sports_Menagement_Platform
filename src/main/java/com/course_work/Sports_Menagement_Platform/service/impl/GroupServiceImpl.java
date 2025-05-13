package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Group;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.repositories.GroupRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GroupService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.MatchService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final StageService stageService;
    private final TeamRepository teamRepository;

    public GroupServiceImpl(GroupRepository groupRepository, StageService stageService, 
                          TeamRepository teamRepository) {
        this.groupRepository = groupRepository;
        this.stageService = stageService;
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Group> getGroups(UUID tournamentId) {
        Stage stage = stageService.createGroupStageIfNotExists(tournamentId);
        System.out.println("DEBUG: Stage for tournament " + tournamentId + ": " + stage);
        if (stage == null) {
            System.out.println("DEBUG: No stage found for tournament " + tournamentId);
            return new ArrayList<>();
        }
        List<Group> groups = groupRepository.findByStageId(stage.getId());
        System.out.println("DEBUG: Found groups for stage " + stage.getId() + ": " + groups);
        System.out.println("DEBUG: Group names: " + groups.stream().map(Group::getName).collect(Collectors.toList()));
        return groups;
    }

    @Override
    public void updateGroupTeams(UUID stageId, Map<String, List<UUID>> groups) {
        List<UUID> allIds = groups.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (allIds.size() != new HashSet<>(allIds).size()) {
            throw new RuntimeException("Одна команда может быть только в одной группе");
        }
        if (groups.values().stream().anyMatch(i -> i.size() == 1)) {
            throw new RuntimeException("Нельзя создать группу с одной командой");
        }

        Stage stage = stageService.getStageById(stageId);
        //TODO: проверки, что все валидно: этап является групповым и команды участвуют в чемпионате
        List<Group> groupsToDelete = getGroups(stage.getTournament().getId());
        groupsToDelete.stream().forEach(i -> groupRepository.delete(i));

        for (Map.Entry<String, List<UUID>> groupEntry : groups.entrySet()) {
            List<Team> teams = teamRepository.findAllById(groupEntry.getValue());
            Group group = Group.builder().stage(stage).name(groupEntry.getKey()).teams(teams).build();
            groupRepository.save(group);
        }

    }
}
