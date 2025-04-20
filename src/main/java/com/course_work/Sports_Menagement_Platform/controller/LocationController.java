package com.course_work.Sports_Menagement_Platform.controller;

import com.course_work.Sports_Menagement_Platform.data.models.Location;
import com.course_work.Sports_Menagement_Platform.data.models.User;
import com.course_work.Sports_Menagement_Platform.dto.LocationCreationDTO;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/all/{tournament_id}")
    public String newLocation(@AuthenticationPrincipal User user, @PathVariable UUID tournament_id, Model model){
        List<Location> locationList = locationService.getLocationsByTournamentId(tournament_id);
        model.addAttribute("locations", locationList);

        model.addAttribute("location", new LocationCreationDTO());
        model.addAttribute("tournament_id", tournament_id);
        return "locations/all_locations";

    }


    @PostMapping("/create/{tournament_id}")
    public String createNewLocation(@AuthenticationPrincipal User user, @PathVariable UUID tournament_id, @Valid @ModelAttribute("location") LocationCreationDTO locationCreationDTO, BindingResult bindingResult, RedirectAttributes redirectAttributes){
        if (bindingResult.hasErrors()) {
            return "locations/all_locations";
        }
        try {
            locationService.createLocation(locationCreationDTO, tournament_id);
            redirectAttributes.addFlashAttribute("success", "Локация успещно создана");
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }

        return "redirect:/location/all/" + tournament_id.toString();
    }


}
