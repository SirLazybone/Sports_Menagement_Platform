package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.dto.StageCreationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.StageRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
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
}
