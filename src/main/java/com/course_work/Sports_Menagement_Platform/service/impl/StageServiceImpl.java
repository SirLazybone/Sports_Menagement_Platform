package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;
import com.course_work.Sports_Menagement_Platform.repositories.StageRepository;
import com.course_work.Sports_Menagement_Platform.repositories.GroupRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageStatusService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StageServiceImpl implements StageService {
    private final StageRepository stageRepository;
    private final TournamentService tournamentService;
    private final TeamService teamService;
    private final GroupRepository groupRepository;
    private final TeamRepository teamRepository;
    private final StageStatusService stageStatusService;

    public StageServiceImpl(StageRepository stageRepository, TournamentService tournamentService, TeamService teamService, GroupRepository groupRepository, TeamRepository teamRepository, StageStatusService stageStatusService) {
        this.stageRepository = stageRepository;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.groupRepository = groupRepository;
        this.teamRepository = teamRepository;
        this.stageStatusService = stageStatusService;
    }

    @Override
    public void createStage(StageCreationDTO stageDTO, UUID tournamentId) {
        Optional<Stage> optionalStage = stageRepository.findByPlaceAndTournamentId(stageDTO.getBestPlace(), stageDTO.getWorstPlace(), tournamentId);
        if (optionalStage.isPresent()) {
            throw new RuntimeException("Этап с такмим параметрами уже существует");
        }
        Tournament tournament = tournamentService.getById(tournamentId);
        Stage stage = Stage.builder()
                .bestPlace(stageDTO.getBestPlace())
                .worstPlace(stageDTO.getWorstPlace())
                .tournament(tournament)
                .isPublished(false).build();
        stageRepository.save(stage);
    }

    @Override
    public List<Stage> getStagesByTournament(UUID tournamentId) {
        return stageRepository.findAllStageByTournamentId(tournamentId);
    }

    @Override
    public List<Team> getTeamsByStageId(UUID stageId) {
        Stage stage = getStageById(stageId);
        Tournament tournament = stage.getTournament();
        return tournamentService.getAllTeamsByTournamentId(tournament.getId());
    }

    @Override
    public Stage getStageById(UUID stageId) {
        return stageRepository.findById(stageId).orElseThrow(() -> new RuntimeException("Нет такого этапа"));
    }

    @Override
    public Tournament getTournamentByStage(UUID stageId) {
        return stageRepository.findByStageId(stageId).orElseThrow(() -> new RuntimeException("Нет такого турнира"));
    }

    @Override
    public void publishStage(UUID stageId) {
        Stage stage = getStageById(stageId);
        stage.setPublished(true);
        stageRepository.save(stage);
    }

    @Override
    public void createClassicScheme(Tournament tournament) {
        Stage groupStage = Stage.builder().bestPlace(0).worstPlace(0).isPublished(false).tournament(tournament).build();
        Stage firstStage = Stage.builder().bestPlace(1).worstPlace(8).isPublished(false).tournament(tournament).build();
        Stage secondStage = Stage.builder().bestPlace(1).worstPlace(4).isPublished(false).tournament(tournament).build();
        Stage finStage = Stage.builder().bestPlace(1).worstPlace(2).isPublished(false).tournament(tournament).build();

        stageRepository.save(groupStage);
        stageRepository.save(firstStage);
        stageRepository.save(secondStage);
        stageRepository.save(finStage);
    }

    @Override
    public String getStageName(int bestPlace, int worstPlace) {
        if (worstPlace == 0) {
            return "Групповой этап";
        } else if (worstPlace == 8 && bestPlace == 1) {
            return "1/8 финала";
        } else if (worstPlace == 4 && bestPlace == 1) {
            return "1/4 финала";
        } else if (worstPlace == 2 && bestPlace == 1) {
            return "Финал";
        } else {
            return "Дополнительные матчи";
        }
    }


    @Override
    public int getTeamsCountInPlayOff(Tournament tournament) {
        // -1 if unknown
        List<Integer> stagesWorstPlaces = tournament.getStages().stream().filter(i -> i.getBestPlace() == 1).map(i -> i.getWorstPlace()).collect(Collectors.toList());
        return Collections.max(stagesWorstPlaces);
    }

    @Override
    @Transactional
    public void createGroups(GroupsDTO groupsDTO) {
        if (groupsDTO.getGroups().isEmpty()) {
            throw new RuntimeException("No groups provided");
        }

        Stage stage = getStageById(groupsDTO.getGroups().get(0).getStageId());
        if (!isGroupStage(stage.getId())) {
            throw new RuntimeException("This stage is not a group stage");
        }

        // Check for duplicate group names
        Set<String> groupNames = new HashSet<>();
        for (GroupDTO groupDTO : groupsDTO.getGroups()) {
            if (!groupNames.add(groupDTO.getName())) {
                throw new RuntimeException("Duplicate group name: " + groupDTO.getName());
            }
        }

        // Create all groups in a single transaction
        for (GroupDTO groupDTO : groupsDTO.getGroups()) {
            Group group = Group.builder()
                    .name(groupDTO.getName())
                    .stage(stage)
                    .teams(new ArrayList<>())
                    .build();
            groupRepository.save(group);
        }
    }


    @Override
    public Stage createGroupStageIfNotExists(UUID tournamentId) {
        Optional<Stage> optional =  stageRepository.findByPlaceAndTournamentId(0, 0, tournamentId);
        if (optional.isPresent()) return optional.get();
        Stage newStage = Stage.builder().bestPlace(0).worstPlace(0).isPublished(false).tournament(tournamentService.getById(tournamentId)).build();
        return stageRepository.save(newStage);
    }

    @Override
    public List<Group> getGroupsByStage(UUID stageId) {
        Stage stage = getStageById(stageId);
        if (!isGroupStage(stageId)) {
            throw new RuntimeException("This stage is not a group stage");
        }

        return groupRepository.findByStageId(stageId);
    }

    @Override
    @Transactional
    public void addTeamsToGroup(GroupDTO groupDTO) {
        Stage stage = getStageById(groupDTO.getStageId());
        if (!isGroupStage(stage.getId())) {
            throw new RuntimeException("This stage is not a group stage");
        }

        Group group = groupRepository.findByStageIdAndName(stage.getId(), groupDTO.getName())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        /*if (group.getTeams().size() + groupDTO.getTeamIds().size() > group.getMaxTeams()) {
            throw new RuntimeException("Adding these teams would exceed the group's maximum team limit");
        }*/

        List<Team> teams = groupDTO.getTeamIds().stream()
                .map(teamService::getById)
                .collect(Collectors.toList());

        // Check if any team is already in another group
        for (Team team : teams) {
            List<Group> existingGroups = groupRepository.findByStageId(stage.getId());
            for (Group existingGroup : existingGroups) {
                if (existingGroup.getTeams().contains(team)) {
                    throw new RuntimeException("Team " + team.getName() + " is already in group " + existingGroup.getName());
                }
            }
        }

        group.getTeams().addAll(teams);
        groupRepository.save(group);
    }

    @Override
    public boolean isGroupStage(UUID stageId) {
        Stage stage = getStageById(stageId);
        return stage.getBestPlace() == 0 && stage.getWorstPlace() == 0;
    }

    @Override
    public Stage getStageByGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        return group.getStage();
    }

    @Override
    public void removeTeamFromGroup(UUID groupId, UUID teamId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (!group.getTeams().contains(team)) {
            throw new RuntimeException("Team is not in this group");
        }
        
        group.getTeams().remove(team);
        groupRepository.save(group);
    }

    @Override
    public Stage getGroupStage(UUID id) {
        return createGroupStageIfNotExists(id);
    }


    @Override
    public StageStatus getStageStatus(Stage stage) {
        return stageStatusService.getStageStatus(stage);
    }


    @Override
    public List<Team> getTeamsForPlatOffStage(Stage stage) {
        //TODO: исправить
        int maxPlayOffPlace = Collections.max(stage.getTournament().getStages().stream().filter(i -> i.getBestPlace() > 0).map(i -> i.getWorstPlace()).collect(Collectors.toList()));
        if (stage.getWorstPlace() == maxPlayOffPlace) {
           return stage.getTournament().getTeamTournamentList().stream().filter(i -> i.isGoToPlayOff()).map(i -> i.getTeam()).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
