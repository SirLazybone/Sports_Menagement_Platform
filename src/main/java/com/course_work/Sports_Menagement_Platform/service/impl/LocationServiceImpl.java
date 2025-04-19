package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.Location;
import com.course_work.Sports_Menagement_Platform.dto.LocationCreationDTO;
import com.course_work.Sports_Menagement_Platform.repositories.LocationRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.TournamentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final ObjectMapper objectMapper;

    private final TournamentService tournamentService;
    public LocationServiceImpl(LocationRepository locationRepository, ObjectMapper objectMapper, TournamentService tournamentService) {
        this.locationRepository = locationRepository;
        this.objectMapper = objectMapper;
        this.tournamentService = tournamentService;
    }
    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public Location getById(UUID locationId) {
        return locationRepository.findById(locationId).orElseThrow(() -> new RuntimeException("Нет такое локации"));
    }

    @Override
    public void loadLocationsFromJson() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("locations.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл cities.json не найден");
            }

            List<Location> locations = objectMapper.readValue(inputStream, new TypeReference<List<Location>>() {});

            for (Location location : locations) {
                if (!locationRepository.existsByName(location.getName())) {
                    locationRepository.save(location);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении JSON", e);
        }
    }


    @Override
    public Location createLocation(LocationCreationDTO locationCreationDTO, UUID tournamentId) {
        Location location = Location.builder().name(locationCreationDTO.getName()).address(locationCreationDTO.getAddress()).tournament(tournamentService.getById(tournamentId)).build();
        return locationRepository.save(location);

    }
}
