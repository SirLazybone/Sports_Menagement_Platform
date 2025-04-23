package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RatingLineDTO {
    UUID teamTournamentId;
    String teamName;
    boolean goesToPlayOff;
    int scoredGoals;
    int missedGoals;
    int diffGoals;
    int matchesCount;
    int winCount;
    int loseCount;
    int drawCount;
    int points;
    
    // Hockey specific fields
    int wonByBullets;
    int lostByBullets;
    
    // Volleyball specific fields
    int wonSets;
    int lostSets;
    double setsRatio;
    double goalsRatio;
}
