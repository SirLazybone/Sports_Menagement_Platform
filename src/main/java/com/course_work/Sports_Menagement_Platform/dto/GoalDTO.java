package com.course_work.Sports_Menagement_Platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalDTO {
    private UUID matchId;
    private UUID teamId;
    private UUID playerId;
    private int time;
    private boolean isPenalty;
}
