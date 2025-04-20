package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Location;
import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.data.models.Stage;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.MatchRepository;
import com.course_work.Sports_Menagement_Platform.repositories.SlotRepository;
import com.course_work.Sports_Menagement_Platform.repositories.TournamentRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.SlotService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {
    private final SlotRepository slotRepository;
    private final MatchRepository matchRepository;
    private final LocationService locationService;
    private final TournamentRepository tournamentRepository;
    public SlotServiceImpl(SlotRepository slotRepository, MatchRepository matchRepository, LocationService locationService, TournamentRepository tournamentRepository) {
        this.slotRepository = slotRepository;
        this.matchRepository = matchRepository;
        this.locationService = locationService;
        this.tournamentRepository = tournamentRepository;
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



    @Override  //Свободные слоты и выбранные для этой stage
    public List<Slot> getAllSlotsForStage(Stage stage) {
        List<Slot> allSlots = getAllByTournament(stage.getTournament().getId());
        List<Slot> freeSlots = new ArrayList<>();
        for (Slot slot : allSlots) {
            if (slot.getMatches().isEmpty() || slot.getMatches().get(0).getStage().equals(stage.getId())) {
                freeSlots.add(slot);
            }
        }
        return freeSlots;
    }

    @Override
    public List<Slot> getAllByTournament(UUID tournamentId) {
        List<Location> locationList = tournamentRepository.getById(tournamentId).getLocations();
        List<List<Slot>> slotsListsList = locationList.stream().map(i -> i.getSlots()).collect(Collectors.toList());
        return slotsListsList.stream().flatMap(List::stream).collect(Collectors.toList());

    }

    @Override
    public void deleteSlot(UUID id) {
        slotRepository.deleteById(id);
    }

}
