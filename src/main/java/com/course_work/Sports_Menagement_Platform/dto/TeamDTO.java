package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamDTO {
    @NotBlank(message = "name can't be empty")
    String name;
    Sport sport;
}
