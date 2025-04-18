package com.course_work.Sports_Menagement_Platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AfterMatchPenaltyDTO {
    private UUID matchId;
    private UUID team1PlayerId;
    private UUID team2PlayerId;
    
    @JsonProperty("isTeam1Scored")
    private boolean isTeam1Scored;
    
    @JsonProperty("isTeam2Scored")
    private boolean isTeam2Scored;
}