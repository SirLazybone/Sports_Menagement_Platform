package com.course_work.Sports_Menagement_Platform.dto;

import java.util.List;
import java.util.UUID;

public class AfterMatchPenaltiesListDTO {
    private UUID matchId;
    private List<AfterMatchPenaltyDTO> penalties;

    public UUID getMatchId() {
        return matchId;
    }

    public void setMatchId(UUID matchId) {
        this.matchId = matchId;
    }

    public List<AfterMatchPenaltyDTO> getPenalties() {
        return penalties;
    }

    public void setPenalties(List<AfterMatchPenaltyDTO> penalties) {
        this.penalties = penalties;
    }
} 