package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SportDTO {
    Sport hockey = Sport.HOCKEY;
    Sport basketball = Sport.BASKETBALL;
    Sport football = Sport.FOOTBALL;
    Sport volleyball = Sport.VOLLEYBALL;
}
