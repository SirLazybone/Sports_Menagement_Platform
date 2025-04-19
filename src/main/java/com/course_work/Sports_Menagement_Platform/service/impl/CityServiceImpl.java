package com.course_work.Sports_Menagement_Platform.service.impl;

import com.course_work.Sports_Menagement_Platform.data.models.City;
import com.course_work.Sports_Menagement_Platform.repositories.CityRepository;
import com.course_work.Sports_Menagement_Platform.service.interfaces.CityService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CityServiceImpl implements CityService {
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public CityServiceImpl(CityRepository cityRepository, ObjectMapper objectMapper) {
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void loadCitiesFromJson() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("cities.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл cities.json не найден");
            }

            List<City> cities = objectMapper.readValue(inputStream, new TypeReference<List<City>>() {});

            for (City city : cities) {
                if (!cityRepository.existsByName(city.getName())) {
                    cityRepository.save(city);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении JSON", e);
        }
    }


    @Override
    public List<City> getCities(){
        return cityRepository.findAll();
    }
}
