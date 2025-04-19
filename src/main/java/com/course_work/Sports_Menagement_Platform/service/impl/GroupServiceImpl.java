package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Group;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.repositories.GroupRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.GroupService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final StageService stageService;
    private final TeamRepository teamRepository;
    public GroupServiceImpl(GroupRepository groupRepository, StageService stageService, TeamRepository teamRepository) {
        this.groupRepository = groupRepository;
        this.stageService = stageService;
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Group> getGroups(UUID tournamentId) {
        Stage stage = stageService.createGroupStageIfNotExists(tournamentId);
        return groupRepository.findByStageId(stage.getId());
    }

    @Override
    public void updateGroupTeams(UUID stageId, Map<String, List<UUID>> groups) {
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
