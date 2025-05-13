package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupDTO;
import com.course_work.Sports_Menagement_Platform.dto.GroupsDTO;
import com.course_work.Sports_Menagement_Platform.repositories.StageRepository;
import com.course_work.Sports_Menagement_Platform.repositories.GroupRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    @Transactional
    public void publishStage(UUID stageId) {
        Stage stage = getStageById(stageId);
        
        // Check if all matches have slots assigned
        List<Match> matches = stage.getMatches();
        if (matches == null || matches.isEmpty()) {
            throw new RuntimeException("Нельзя опубликовать этап: не все данные заполнены");
        }
        List<Match> matchesWithoutSlots = matches.stream()
            .filter(match -> match.getSlot() == null)
            .collect(Collectors.toList());

        if (!matchesWithoutSlots.isEmpty()) {
            throw new RuntimeException("Нельзя опубликовать этап: не все матчи имеют назначенные слоты");
        }

        // For group stages, ensure matches are properly synchronized
        if (isGroupStage(stageId)) {
            // Get existing matches and groups
            List<Match> existingMatches = stage.getMatches();
            List<Group> groups = getGroupsByStage(stageId);
            
            // Create a set of valid team pairs from groups
            Set<Pair<UUID, UUID>> validTeamPairs = new HashSet<>();
            for (Group group : groups) {
                List<Team> teams = group.getTeams();
                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        Team team1 = teams.get(i);
                        Team team2 = teams.get(j);
                        // Store pairs in a consistent order (smaller UUID first)
                        if (team1.getId().compareTo(team2.getId()) < 0) {
                            validTeamPairs.add(Pair.of(team1.getId(), team2.getId()));
                        } else {
                            validTeamPairs.add(Pair.of(team2.getId(), team1.getId()));
                        }
                    }
                }
            }
            
            // Remove matches that don't correspond to valid team pairs
            if (existingMatches != null) {
                existingMatches.removeIf(match -> {
                    if (match.getTeam1() == null || match.getTeam2() == null) {
                        return true;
                    }
                    UUID team1Id = match.getTeam1().getId();
                    UUID team2Id = match.getTeam2().getId();
                    Pair<UUID, UUID> pair = team1Id.compareTo(team2Id) < 0 
                        ? Pair.of(team1Id, team2Id) 
                        : Pair.of(team2Id, team1Id);
                    return !validTeamPairs.contains(pair);
                });
            }
            
            // Create any missing matches for valid team pairs
            for (Group group : groups) {
                List<Team> teams = group.getTeams();
                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        Team team1 = teams.get(i);
                        Team team2 = teams.get(j);
                        UUID team1Id = team1.getId();
                        UUID team2Id = team2.getId();
                        Pair<UUID, UUID> pair = team1Id.compareTo(team2Id) < 0 
                            ? Pair.of(team1Id, team2Id) 
                            : Pair.of(team2Id, team1Id);
                            
                        // Check if match already exists
                        boolean matchExists = existingMatches.stream()
                            .anyMatch(m -> {
                                UUID m1 = m.getTeam1().getId();
                                UUID m2 = m.getTeam2().getId();
                                return (m1.equals(team1Id) && m2.equals(team2Id)) || 
                                       (m1.equals(team2Id) && m2.equals(team1Id));
                            });
                            
                        if (!matchExists) {
                            Match match = Match.builder()
                                .stage(stage)
                                .team1(team1)
                                .team2(team2)
                                .isResultPublished(false)
                                .build();
                            existingMatches.add(match);
                        }
                    }
                }
            }
        }
        
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

        // Get existing groups in this stage
        List<Group> existingGroups = groupRepository.findByStageId(stage.getId());
        Set<String> existingGroupNames = existingGroups.stream()
            .map(Group::getName)
            .collect(Collectors.toSet());

        // Check for duplicate group names within this stage
        Set<String> newGroupNames = new HashSet<>();
        for (GroupDTO groupDTO : groupsDTO.getGroups()) {
            if (existingGroupNames.contains(groupDTO.getName()) || !newGroupNames.add(groupDTO.getName())) {
                throw new RuntimeException("Duplicate group name within the same tournament: " + groupDTO.getName());
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
    @Transactional
    public Stage createGroupStageIfNotExists(UUID tournamentId) {
        Tournament tournament = tournamentService.getById(tournamentId);
        System.out.println("DEBUG: Creating/checking group stage for tournament: " + tournament.getName());
        
        // Check if registration deadline has passed
        if (tournament.getRegisterDeadline().isAfter(LocalDate.now())) {
            System.out.println("DEBUG: Registration deadline not passed: " + tournament.getRegisterDeadline());
            return null;  // Registration not finished yet
        }
        
        // Check for pending applications
        boolean hasPendingApplications = tournament.getTeamTournamentList().stream()
            .anyMatch(tt -> tt.getApplicationStatus() == ApplicationStatus.PENDING);
        if (hasPendingApplications) {
            System.out.println("DEBUG: Has pending applications");
            return null;  // Cannot create group stage while there are pending applications
        }
        
        // Check if group stage already exists
        Optional<Stage> existingStage = stageRepository.findByPlaceAndTournamentId(0, 0, tournamentId);
        System.out.println("DEBUG: Existing stage check: " + existingStage);
        if (existingStage.isPresent()) {
            System.out.println("DEBUG: Found existing stage: " + existingStage.get().getId());
            return existingStage.get();
        }
        
        // Create new group stage
        System.out.println("DEBUG: Creating new group stage");
        Stage newStage = Stage.builder()
            .bestPlace(0)
            .worstPlace(0)
            .isPublished(false)
            .tournament(tournament)
            .build();
        
        Stage savedStage = stageRepository.save(newStage);
        System.out.println("DEBUG: Created new stage: " + savedStage.getId());
        return savedStage;
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
    public Stage getPrevious(Stage stage) {
        if (stage.getBestPlace() == 1) {
            Stage result = stage.getTournament().getStages().stream().filter(stage1 -> stage1.getBestPlace() == 1 && stage1.getWorstPlace() == stage.getWorstPlace() * 2).collect(Collectors.toList()).get(0);
            return result;
        }
        else {
            Stage result = stage.getTournament().getStages().stream().filter(stage1 -> stage1.getBestPlace() <= stage.getBestPlace() && stage1.getWorstPlace() >= stage.getWorstPlace()
                    && stage1.getWorstPlace() - stage1.getBestPlace() == 2 * (stage.getWorstPlace() - stage.getBestPlace())).collect(Collectors.toList()).get(0);
            return result;
        }
    }




    @Override
    public List<Stage> getAdditionalStages(UUID tournamentId) {
        return getStagesByTournament(tournamentId).stream().filter(i -> i.getBestPlace() < 0).collect(Collectors.toList());
    }


    @Override
    public Stage createStageForAdditionalMatch(UUID tournamentId) {
        List<Integer> stageIndexes = getStagesByTournament(tournamentId).stream().filter(i -> i.getBestPlace() < 0).map(i -> i.getBestPlace()).collect(Collectors.toList());
        if (stageIndexes.isEmpty()) {
            Stage stage = Stage.builder().tournament(tournamentService.getById(tournamentId)).bestPlace(-1).worstPlace(-1).isPublished(false).build();
            stage = stageRepository.save(stage);
            return stage;
        }
        int index = Collections.min(stageIndexes) - 1;
        Stage stage = Stage.builder().tournament(tournamentService.getById(tournamentId)).bestPlace(index).worstPlace(index).isPublished(false).build();
        return stageRepository.save(stage);

    }
}
