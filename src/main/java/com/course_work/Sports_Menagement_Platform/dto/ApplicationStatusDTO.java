package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationStatusDTO {
    private ApplicationStatus pending = ApplicationStatus.PENDING;
    private ApplicationStatus accepted = ApplicationStatus.ACCEPTED;
    private ApplicationStatus declined = ApplicationStatus.DECLINED;
    private ApplicationStatus canceled = ApplicationStatus.CANCELED;

}
