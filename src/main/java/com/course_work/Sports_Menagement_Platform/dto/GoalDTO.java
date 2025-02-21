package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoalDTO {
    private boolean isPenalty;
    private int time;
    private int points;
    private int setNumber;
    private UUID teamId;
    private UUID userId;
    private UUID matchId;
}
