package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdditionalMatchDTO {
    private UUID team1;
    private UUID team2;
    private UUID slot;
}
