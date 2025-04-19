package com.course_work.Sports_Menagement_Platform.service.interfaces;

import com.course_work.Sports_Menagement_Platform.data.models.City;

import java.util.List;

public interface CityService {
    void loadCitiesFromJson();

    List<City> getCities();
}
