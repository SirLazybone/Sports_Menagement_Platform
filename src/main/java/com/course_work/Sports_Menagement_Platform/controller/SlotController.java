package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Location;
import com.course_work.Sports_Menagement_Platform.data.models.Slot;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.SlotCreationDTO;
import com.course_work.Sports_Menagement_Platform.dto.SlotDTO;
import com.course_work.Sports_Menagement_Platform.exception.AccessDeniedException;
import com.course_work.Sports_Menagement_Platform.exception.ResourceNotFoundException;
import com.course_work.Sports_Menagement_Platform.service.impl.AccessService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.SlotService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/slot")
public class SlotController {
    private final SlotService slotService;
    private final LocationService locationService;
    private final TournamentService tournamentService;
    private final AccessService accessService;

    public SlotController(SlotService slotService, LocationService locationService,
                          TournamentService tournamentService, AccessService accessService) {
        this.slotService = slotService;
        this.locationService = locationService;
        this.tournamentService = tournamentService;
        this.accessService = accessService;
    }

    @GetMapping("/all/{tournamentId}")
    public String allSlots(Model model, @PathVariable UUID tournamentId, @AuthenticationPrincipal User user) {
        try {
            tournamentService.getById(tournamentId);
        } catch (RuntimeException e) {
            throw new ResourceNotFoundException("Нет такого турнира");
        }
        try {
            if (!accessService.isUserOrgOfTournament(user.getId(), tournamentId)) {
                throw new AccessDeniedException("У вас нет доступа");
            }
        } catch (RuntimeException e) {
            throw new AccessDeniedException("У вас нет доступа");
        }
        List<Slot> slots = slotService.getAllByTournament(tournamentId);
        List<SlotDTO> slotDTOS = slots.stream().map(i -> SlotDTO.builder().id(i.getId()).
                time(i.getTime()).date(i.getDate()).hasMatches(!i.getMatches().isEmpty()).
                locationId(i.getLocation().getId()).locationName(i.getLocation().getName()).build()).collect(Collectors.toList());
        model.addAttribute("tournamentId", tournamentId);
        model.addAttribute("slotDTO", slotDTOS);
        model.addAttribute("slotCreation", new SlotCreationDTO());
        model.addAttribute("locations", locationService.getLocationsByTournamentId(tournamentId));

        return "slot/all_slots";

    }

    @DeleteMapping("/delete/{slotID}")
    public String deleteSlot(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        //TODO: проверка и вылетает 405
        UUID tournament_id = slotService.getById(id).getLocation().getTournament().getId();
        slotService.deleteSlot(id);
        return "redirect:/slot/all/" + tournament_id.toString();
    }


    @PostMapping("/create")
    public String createSlot(@Valid @ModelAttribute("slot") SlotCreationDTO slotDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "tournament/create_slot";
        }
        try {
            slotService.createSlot(slotDTO);
            redirectAttributes.addFlashAttribute("success", "Слот успешно создан");
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        UUID tournamentId = locationService.getById(slotDTO.getLocation()).getTournament().getId();
        return "redirect:/slot/all/" + tournamentId.toString();
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

    @PostMapping("/slot/delete/{slotId}/{tournamentId}")
    public String deleteSlot(@PathVariable UUID slotId, @PathVariable UUID tournamentId, RedirectAttributes redirectAttributes) {
        try {
            slotService.deleteSlot(slotId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/slot/all/" + tournamentId.toString();
    }
}
