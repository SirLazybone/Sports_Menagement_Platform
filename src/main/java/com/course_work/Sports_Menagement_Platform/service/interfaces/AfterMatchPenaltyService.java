package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.AfterMatchPenalty;
import com.course_work.Sports_Menagement_Platform.dto.AfterMatchPenaltyDTO;

import java.util.List;
import java.util.UUID;

public interface AfterMatchPenaltyService {
    List<AfterMatchPenalty> getPenaltiesByMatch(UUID matchId);
    void addPenalties(AfterMatchPenaltyDTO penaltyDTO);
    boolean hasPenalties(UUID matchId);
}
