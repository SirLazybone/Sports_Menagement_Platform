package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupDTO {
    private UUID stageId;
    
    @NotBlank(message = "Название группы обязательно")
    private String name;
    
    @NotNull(message = "Количество команд обязательно")
    private int maxTeams;
    
    private List<UUID> teamIds;
}
