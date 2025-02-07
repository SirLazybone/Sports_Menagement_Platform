package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrgComDTO {
    @NotBlank(message = "Имя орг комитета не может быть пустым")
    private String name;
}
