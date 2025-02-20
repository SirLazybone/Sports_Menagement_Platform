package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.Location;

import java.util.List;
import java.util.UUID;

public interface LocationService {
    List<Location> getAllLocations();

    Location getById(UUID locationId);

    void loadLocationsFromJson();
}
