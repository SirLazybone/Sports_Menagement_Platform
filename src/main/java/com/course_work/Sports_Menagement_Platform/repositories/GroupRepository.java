package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Group;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByStageId(UUID stageId);
    
    @Query("SELECT g FROM Group g WHERE g.stage.id = :stageId AND g.name = :name")
    Optional<Group> findByStageIdAndName(@Param("stageId") UUID stageId, @Param("name") String name);
}
