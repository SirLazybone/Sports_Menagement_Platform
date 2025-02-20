package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StageCreationDTO {
    @NotBlank
    private String name;
    @NotNull
    private int bestPlace;
    @NotNull
    private int worstPlace;

}
