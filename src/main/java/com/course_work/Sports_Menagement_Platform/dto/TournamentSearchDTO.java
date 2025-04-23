package com.course_work.Sports_Menagement_Platform.dto;

import com.course_work.Sports_Menagement_Platform.data.enums.Sport;
import lombok.*;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentSearchDTO {
    private String name;
    private List<UUID> cities;
    private List<Sport> sports;
    private int teamSizeFromInt;
    private int teamSizeToInt;
    private boolean registrationGoing;

    @ModelAttribute("sports")
    public void setSports(List<String> sportStrings) {
        if (sportStrings != null) {
            this.sports = sportStrings.stream()
                    .map(Sport::valueOf)
                    .toList();
        }
        else{
            this.sports = new ArrayList<>();
        }
    }

    @ModelAttribute("cities")
    public void setCities(List<String> cityStrings) {
        if (cityStrings != null) {
            this.cities = cityStrings.stream()
                    .map(i -> UUID.fromString(i))
                    .toList();
        }else{
            this.cities = new ArrayList<>();
        }
    }


    @ModelAttribute("teamSizeFrom")
    public void setTeamSizeFrom(String value) {
        if (value == "") teamSizeFromInt = 0;
        else {
            try {
                teamSizeFromInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                teamSizeFromInt = 0;
            }
        }
    }


    @ModelAttribute("teamSizeTo")
    public void setTeamSizeTo(String value) {
        if (value.equals("0")) teamSizeToInt = 1000000000;
        else {
            try {
                teamSizeToInt = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                teamSizeToInt = 1000000000;
            }
        }
    }


    @ModelAttribute("teamSizeFrom")
    public String getTeamSizeFrom() {
        if (teamSizeFromInt == 0) return "";
        return Integer.toString(teamSizeFromInt);
    }

    @ModelAttribute("teamSizeTo")
    public String getTeamSizeTo() {
        if (teamSizeToInt == 0 || teamSizeToInt == 1000000000) return  "";
        return Integer.toString(teamSizeToInt);

    }

}
