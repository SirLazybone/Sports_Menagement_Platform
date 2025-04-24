package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProlongRegDTO {
    private LocalDate registerDeadline;
}
