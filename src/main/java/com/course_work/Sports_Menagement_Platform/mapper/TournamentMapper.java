package com.course_work.Sports_Menagement_Platform.mapper;

import com.course_work.Sports_Menagement_Platform.data.models.Tournament;
import com.course_work.Sports_Menagement_Platform.dto.TournamentDTO;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {
    public Tournament DTOToEntity(TournamentDTO tournamentDTO) {
        return Tournament.builder()
                .name(tournamentDTO.getName())
                .sport(tournamentDTO.getSport())
                .minMembers(tournamentDTO.getMinMembers())
                .registerDeadline(tournamentDTO.getRegisterDeadline())
                .description(tournamentDTO.getDescription()).build();
    }

    public TournamentDTO EntityToDTO(Tournament tournament) {
        return TournamentDTO.builder()
                .name(tournament.getName())
                .sport(tournament.getSport())
                .cityName(tournament.getCity().getName())
                .minMembers(tournament.getMinMembers())
                .registerDeadline(tournament.getRegisterDeadline())
                .description(tournament.getDescription()).build();
    }
}
