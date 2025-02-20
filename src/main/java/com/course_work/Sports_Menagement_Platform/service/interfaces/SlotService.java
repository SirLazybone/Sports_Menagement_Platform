package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;

import java.util.UUID;

public interface SlotService {

    void createSlot(SlotCreationDTO slotDTO, UUID matchId);

    Slot getById(UUID slotId);
}
