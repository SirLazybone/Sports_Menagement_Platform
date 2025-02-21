package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDTO {
    @NotNull
    private String index;
    @NotBlank
    private int countTeams;
}
