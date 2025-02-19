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
public class InvitationTeamDTO {
    @NotBlank(message = "Телефон не может быть пустым")
    String tel;
    Boolean isCap;
}
