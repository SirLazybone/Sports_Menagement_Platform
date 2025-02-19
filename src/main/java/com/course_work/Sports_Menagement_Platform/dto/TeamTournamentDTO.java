package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamTournamentDTO {
    private String tournamentName;
    private UUID tournamentId;
    private ApplicationStatus applicationStatus;
}
