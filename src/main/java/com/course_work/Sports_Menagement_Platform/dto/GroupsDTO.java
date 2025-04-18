package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.Valid;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupsDTO {
    @Valid
    private List<GroupDTO> groups = new ArrayList<>();
} 