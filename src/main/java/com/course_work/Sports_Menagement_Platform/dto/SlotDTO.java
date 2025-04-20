package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotDTO {
    UUID id;
    LocalTime time;
    LocalDate date;
    UUID locationId;
    String locationName;
    boolean hasMatches;

}
