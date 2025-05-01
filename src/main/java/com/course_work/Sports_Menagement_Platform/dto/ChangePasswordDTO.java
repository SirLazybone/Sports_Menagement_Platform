package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
