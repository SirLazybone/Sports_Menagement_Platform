package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.repositories.SlotRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.SlotService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SlotServiceImpl implements SlotService {
    private final SlotRepository slotRepository;
    private final MatchRepository matchRepository;
    private final LocationService locationService;
    public SlotServiceImpl(SlotRepository slotRepository, MatchRepository matchRepository, LocationService locationService) {
        this.slotRepository = slotRepository;
        this.matchRepository = matchRepository;
        this.locationService = locationService;
    }

    @Override
    public void createSlot(SlotCreationDTO slotDTO) {
        Slot slot = Slot.builder()
                .date(slotDTO.getDate())
                .time(slotDTO.getTime())
                .location(locationService.getById(slotDTO.getLocation())).build();

        slotRepository.save(slot);
    }

    @Override
    public Slot getById(UUID slotId) {
        return slotRepository.findById(slotId).orElseThrow(() -> new RuntimeException("Слот не существует"));
    }

    @Override
    public List<Slot> getAllNotInUse() {
        return slotRepository.findAllNotInUse();
    }

}
