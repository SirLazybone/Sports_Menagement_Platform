package com.course_work.Sports_Menagement_Platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreationDTO {
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @NotBlank(message = "Фамилия не может быть пустой")
    private String surname;
    @NotBlank(message = "Телефон не может быть пустым")
    @Pattern(regexp = "^(\\+7\\s*\\(\\d{3}\\)\\s*\\d{3}-\\d{2}-\\d{2}|\\+7\\d{10})$", 
             message = "Телефон должен быть в формате +7 (XXX) XXX-XX-XX или +7XXXXXXXXXX")
    private String tel;
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
