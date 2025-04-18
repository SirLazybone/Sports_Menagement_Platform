package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentDTO {
    @NotBlank(message = "Не может быть пустым")
    private String name;
    private Sport sport;
    @NotBlank(message = "Не может быть пустым")
    private String orgComName;
    @NotBlank(message = "Не может быть пустым")
    private String cityName;
    private int minMembers;
    private LocalDate registerDeadline;
    private String description;
    private boolean isClassicScheme;
//    private String logo;
}
