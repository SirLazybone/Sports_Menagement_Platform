package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.enums.StageStatus;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;

public interface StageStatusService {
    StageStatus getStageStatus(Stage stage);
}
