package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlotCreationDTO {
    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;

    @NotNull
    private UUID location;
}
