package com.course_work.Sports_Menagement_Platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayOffMatchesDTO {
    private List<String> team1;
    private List<String> team2;
    private List<String> slot;

}
