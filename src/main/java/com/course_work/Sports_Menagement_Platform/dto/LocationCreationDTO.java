package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationCreationDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String address;
}
