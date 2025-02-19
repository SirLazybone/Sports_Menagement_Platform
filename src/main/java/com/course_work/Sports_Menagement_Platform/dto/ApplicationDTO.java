package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationDTO {
    private String teamName;
    private UUID teamId;

    public ApplicationDTO(String teamName) {
        this.teamName = teamName;
    }
}
