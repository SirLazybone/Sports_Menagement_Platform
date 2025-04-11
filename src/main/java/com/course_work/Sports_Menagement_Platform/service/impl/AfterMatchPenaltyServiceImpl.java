package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.AfterMatchPenalty;
import com.course_work.Sports_Menagement_Platform.data.models.Match;
import com.course_work.Sports_Menagement_Platform.data.models.Team;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.repositories.AfterMatchPenaltyRepository;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TeamRepository;
import com.course_work.Sports_Menagement_Platform.repositories.UserRepository;
import com.course_work.Sports_Menagement_Platform.dto.AfterMatchPenaltyDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.AfterMatchPenaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AfterMatchPenaltyServiceImpl implements AfterMatchPenaltyService {

    private final AfterMatchPenaltyRepository afterMatchPenaltyRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public List<AfterMatchPenalty> getPenaltiesByMatch(UUID matchId) {
        return afterMatchPenaltyRepository.findAllByMatchId(matchId);
    }

    @Override
    public void addPenalties(AfterMatchPenaltyDTO penaltyDTO) {
        Match match = matchRepository.findById(penaltyDTO.getMatchId())
                .orElseThrow(() -> new RuntimeException("Match not found"));

        // Handle team 1 penalty
        User team1Player = userRepository.findById(penaltyDTO.getTeam1PlayerId())
                .orElseThrow(() -> new RuntimeException("Team 1 player not found"));
        
        AfterMatchPenalty team1Penalty = AfterMatchPenalty.builder()
                .match(match)
                .team(match.getTeam1())
                .player(team1Player)
                // .roundNumber(penaltyDTO.getRoundNumber())
                .scored(penaltyDTO.isTeam1Scored())
                .build();
        
        afterMatchPenaltyRepository.save(team1Penalty);

        // Handle team 2 penalty
        User team2Player = userRepository.findById(penaltyDTO.getTeam2PlayerId())
                .orElseThrow(() -> new RuntimeException("Team 2 player not found"));
        
        AfterMatchPenalty team2Penalty = AfterMatchPenalty.builder()
                .match(match)
                .team(match.getTeam2())
                .player(team2Player)
                // .roundNumber(penaltyDTO.getRoundNumber())
                .scored(penaltyDTO.isTeam2Scored())
                .build();
        
        afterMatchPenaltyRepository.save(team2Penalty);
    }

    @Override
    public boolean hasPenalties(UUID matchId) {
        return !afterMatchPenaltyRepository.findAllByMatchId(matchId).isEmpty();
    }
}
