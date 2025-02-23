package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.dto.MatchDTO;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.repositories.StageRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TeamService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StageServiceImpl implements StageService {
    private final StageRepository stageRepository;
    private final TournamentService tournamentService;
    public StageServiceImpl(StageRepository stageRepository, TournamentService tournamentService) {
        this.stageRepository = stageRepository;
        this.tournamentService = tournamentService;
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
}
