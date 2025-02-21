package com.course_work.Sports_Menagement_Platform.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchListDTO {
    private List<MatchDTO> newMatches;
}
