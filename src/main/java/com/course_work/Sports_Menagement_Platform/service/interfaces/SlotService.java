package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;

import java.util.List;
import java.util.UUID;

public interface SlotService {

    void createSlot(SlotCreationDTO slotDTO);

    Slot getById(UUID slotId);

    List<Slot> getAllNotInUse();


    //Свободные слоты и выбранные для этой stage
    List<Slot> getAllSlotsForStage(Stage stage);

    List<Slot> getFreeSlots(Tournament tournament);

    List<Slot> getAllByTournament(UUID tournamentId);

    void deleteSlot(UUID id);

}
