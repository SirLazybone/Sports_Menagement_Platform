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
    int goals; //TODO: всякие цифры
}
