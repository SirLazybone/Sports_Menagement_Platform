package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.SlotService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/slot")
public class SlotController {
    private final SlotService slotService;
    private final LocationService locationService;

    public SlotController(SlotService slotService, LocationService locationService) {
        this.slotService = slotService;
        this.locationService = locationService;
    }

    @GetMapping("/create/{matchId}")
    public String createSlotForm(@PathVariable UUID matchId, Model model) {
        model.addAttribute("slot", new SlotCreationDTO());
        model.addAttribute("matchId", matchId);
        model.addAttribute("locations", locationService.getAllLocations());
        return "tournament/create_slot";
    }

    @PostMapping("/create/{matchId}")
    public String createSlot(@PathVariable UUID matchId, @Valid @ModelAttribute("slot") SlotCreationDTO slotDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "tournament/create_slot";
        }
        slotService.createSlot(slotDTO, matchId);
        return "redirect:/tournament/matches/" + matchId;
    }

    @GetMapping("/view/{slotId}")
    public String showSlot(@PathVariable UUID slotId, Model model) {
        Slot slot = null;
        try {
            slot = slotService.getById(slotId);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("slot", slot);
        return "tournament/slot";
    }
}
