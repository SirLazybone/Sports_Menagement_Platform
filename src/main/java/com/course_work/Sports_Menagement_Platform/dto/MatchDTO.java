package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchDTO {
    private UUID stageId;
    private UUID team1Id;
    private UUID team2Id;
}
