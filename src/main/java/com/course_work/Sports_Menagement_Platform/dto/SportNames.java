package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SportNames {
    private final Map<Sport, String> map = Map.of(Sport.FOOTBALL, "Футбол", Sport.BASKETBALL, "Баскетбол", Sport.HOCKEY, "Хоккей", Sport.VOLLEYBALL, "Воллейбол");

}
