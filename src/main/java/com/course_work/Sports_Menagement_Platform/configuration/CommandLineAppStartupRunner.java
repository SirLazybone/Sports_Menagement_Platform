package com.course_work.Sports_Menagement_Platform.configuration;

import com.course_work.Sports_Menagement_Platform.data.enums.*;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.dto.AdditionalMatchDTO;
import com.course_work.Sports_Menagement_Platform.repositories.*;
import com.course_work.Sports_Menagement_Platform.service.impl.TeamServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    @Autowired
    UserServiceImpl userService;

    @Autowired
    TeamRepository teamRepository;


    @Autowired
    SlotRepository slotRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    GroupRepository groupRepository;


    @Autowired
    TournamentRepository tournamentRepository;

    @Autowired
    UserTeamRepository userTeamRepository;

    @Autowired
    MatchService matchService;

    @Autowired
    TeamTournamentRepository teamTournamentRepository;
    @Autowired
    UserOrgComRepository userOrgComRepository;


    @Autowired
    OrgComRepository orgComRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CityService cityService;
    @Autowired
    LocationService locationService;

    private void createFirstUsers() {
        for (int i = 0; i <= 80; i++) {
            User user = null;
            String phone = "+" + Integer.toString(i);
            try {
                user = userService.findByTel(phone);
            } catch (UsernameNotFoundException e) {
                user = User.builder().name("User" + Integer.toString(i))
                        .surname("User" + Integer.toString(i))
                        .tel("+" + Integer.toString(i))
                        .password(passwordEncoder.encode("test"))
                        .role(Role.USER)
                        .build();
                userRepository.save(user);
            }
        }


    }



    private Tournament createOrgComAndChamp() {
        OrgCom orgCom = OrgCom.builder().name("OrgCom").build();
        orgComRepository.save(orgCom);
        UserOrgCom userOrgCom = UserOrgCom.builder().orgCom(orgCom).orgRole(Org.CHIEF).user(userService.findByTel("+0")).isRef(true).invitationStatus(InvitationStatus.ACCEPTED).build();
        userOrgComRepository.save(userOrgCom);


        Tournament tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(0)).minMembers(5).
                name("Tour2").registerDeadline(LocalDate.of(2025, 8, 01)).is_stopped(false).build();
        UUID tour2Id = tournament.getId();
        tournamentRepository.save(tournament);
         tournament = Tournament.builder().sport(Sport.BASKETBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(1)).minMembers(5).
                name("Tour3").registerDeadline(LocalDate.of(2025, 8, 01)).is_stopped(false).build();
        tournamentRepository.save(tournament);
         tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(1)).minMembers(11).
                name("Tour4").registerDeadline(LocalDate.of(2025, 8, 01)).is_stopped(false).build();
        tournamentRepository.save(tournament);
         tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(0)).minMembers(5).
                name("Tour5").registerDeadline(LocalDate.of(2025, 01, 01)).is_stopped(false).build();
        tournamentRepository.save(tournament);
         tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(0)).minMembers(5).
                name("Tour6").registerDeadline(LocalDate.of(2025, 01, 01)).is_stopped(false).build();
        tournamentRepository.save(tournament);


        tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(0)).minMembers(5).
                name("Tour1").registerDeadline(LocalDate.of(2025, 01, 01)).is_stopped(false).build();


        return tournamentRepository.save(tournament);


    }

    private void createTeams(Tournament tournament){
        for (int teamNum = 0; teamNum < 10; teamNum++) {
            Team team = Team.builder().name("Team" + Integer.toString(teamNum + 1)).sport(Sport.FOOTBALL).build();
            team = teamRepository.save(team);
            for (int member = 1; member <= 8; member++) {
                User user = userService.findByTel("+" + Integer.toString(teamNum * 8 + member));
                UserTeam userTeam = UserTeam.builder().isPlaying(true).team(team).user(user).isCap(true).invitationStatus(InvitationStatus.ACCEPTED).build();
                userTeamRepository.save(userTeam);
            }
            TeamTournament teamTournament = TeamTournament.builder().tournament(tournament).team(team).applicationStatus(ApplicationStatus.ACCEPTED).build();
            teamTournamentRepository.save(teamTournament);
        }
        Stage stage = Stage.builder().isPublished(false).bestPlace(0).worstPlace(0).tournament(tournament).build();
        stageRepository.save(stage);

        ArrayList<Team> group1 = new ArrayList<>();
        group1.add(teamRepository.findByName("Team1").get());
        group1.add(teamRepository.findByName("Team2").get());
        group1.add(teamRepository.findByName("Team3").get());
        group1.add(teamRepository.findByName("Team4").get());
        group1.add(teamRepository.findByName("Team5").get());

        ArrayList<Team> group2 = new ArrayList<>();
        group2.add(teamRepository.findByName("Team6").get());
        group2.add(teamRepository.findByName("Team7").get());
        group2.add(teamRepository.findByName("Team8").get());
        group2.add(teamRepository.findByName("Team9").get());
        group2.add(teamRepository.findByName("Team10").get());

        Group groupFinal1 = Group.builder().teams(group1).name("A").stage(stage).build();
        Group groupFinal2 = Group.builder().teams(group2).name("B").stage(stage).build();
        groupRepository.save(groupFinal1);
        groupRepository.save(groupFinal2);

        Stage stage2 = Stage.builder().isPublished(false).bestPlace(0).worstPlace(0).tournament(tournamentRepository.findByName("Tour2")).build();
        stageRepository.save(stage2);
        ArrayList<Team> group21 = new ArrayList<>();
        group21.add(teamRepository.findByName("Team1").get());
        group21.add(teamRepository.findByName("Team2").get());
        group21.add(teamRepository.findByName("Team3").get());
        group21.add(teamRepository.findByName("Team4").get());
        group21.add(teamRepository.findByName("Team5").get());

        ArrayList<Team> group22 = new ArrayList<>();
        group22.add(teamRepository.findByName("Team6").get());
        group22.add(teamRepository.findByName("Team7").get());
        group22.add(teamRepository.findByName("Team8").get());
        group22.add(teamRepository.findByName("Team9").get());
        group22.add(teamRepository.findByName("Team10").get());

        Group groupFinal21 = Group.builder().teams(group21).name("С").stage(stage2).build();
        Group groupFinal22 = Group.builder().teams(group22).name("D").stage(stage2).build();
        groupRepository.save(groupFinal21);
        groupRepository.save(groupFinal22);


        Location location = Location.builder().address("Ленина 1").name("Стадион 1").tournament(tournament).build();
        location = locationRepository.save(location);

        for (int i = 1; i <= 30; i++) {
            Slot slot = Slot.builder().date(LocalDate.of(2025, 7, i)).time(LocalTime.of(15, 00, 0)).location(location).build();
            slot = slotRepository.save(slot);
            if (i < 5) {
                matchService.createAdditionalMatch(tournament.getId(), new AdditionalMatchDTO(teamRepository.findByName("Team1").get().getId(),
                        teamRepository.findByName("Team2").get().getId(), slot.getId()));
            }
        }




        // TODO: Это очень плохо -- потом исправить
        for (int i = 1; i <= 8; i++) {
            TeamTournament teamTournament = teamTournamentRepository.findByTournamentIdAndTeamId(tournament.getId(), teamRepository.findByName("Team" + Integer.toString(i)).get().getId())
                    .get();
            teamTournament.setGoToPlayOff(true);
            teamTournamentRepository.save(teamTournament);
        }
        Stage stageFinal = Stage.builder().isPublished(false).bestPlace(1).worstPlace(8).tournament(tournament).build();
        stageRepository.save(stageFinal);

        TeamTournament teamTournament = teamTournamentRepository.findByTournamentIdAndTeamId(tournament.getId(), teamRepository.findByName("Team" + Integer.toString(1)).get().getId())
                .get();
        teamTournament.setGoToPlayOff(true);
        teamTournamentRepository.save(teamTournament);

    }





    @Override
    public void run(String... args) throws Exception {
        if (cityService.getCities() == null || cityService.getCities().isEmpty()) {
            cityService.loadCitiesFromJson();
        }
        if (locationService.getAllLocations() == null || locationService.getAllLocations().isEmpty()) {
            locationService.loadLocationsFromJson();
        }
        if (userRepository.count() == 0) {
            createFirstUsers();
        }
        if (teamRepository.count() == 0) {
            createTeams(createOrgComAndChamp());
        }
    }
}
