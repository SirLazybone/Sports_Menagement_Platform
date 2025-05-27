package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

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
    private boolean penalty;
    private int points;
    private int setNumber;
}
