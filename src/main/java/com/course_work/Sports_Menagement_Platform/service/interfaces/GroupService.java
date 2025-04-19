package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Group;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface GroupService {
    List<Group> getGroups(UUID tournamentId);

    void updateGroupTeams(UUID stageId, Map<String, List<UUID>> groups);
}
