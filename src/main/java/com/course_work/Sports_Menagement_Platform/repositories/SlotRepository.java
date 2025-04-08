package com.course_work.Sports_Menagement_Platform.repositories;

import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID> {
    @Query("SELECT s FROM Slot s LEFT JOIN Match m ON s.id = m.slot.id WHERE m.slot.id IS NULL")
    List<Slot> findAllNotInUse();
}
