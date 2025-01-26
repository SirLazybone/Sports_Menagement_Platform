package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    private String password;
    private String tel;
}
