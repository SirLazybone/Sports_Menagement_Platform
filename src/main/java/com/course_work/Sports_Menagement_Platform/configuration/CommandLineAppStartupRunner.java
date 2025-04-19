package com.course_work.Sports_Menagement_Platform.configuration;

import com.course_work.Sports_Menagement_Platform.data.enums.*;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.repositories.*;
import com.course_work.Sports_Menagement_Platform.service.impl.TeamServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.impl.UserServiceImpl;
import com.course_work.Sports_Menagement_Platform.service.interfaces.CityService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.LocationService;
import com.course_work.Sports_Menagement_Platform.service.interfaces.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    @Autowired
    UserServiceImpl userService;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    StageRepository stageRepository;

    @Autowired
    GroupRepository groupRepository;


    @Autowired
    TournamentRepository tournamentRepository;

    @Autowired
    UserTeamRepository userTeamRepository;

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
        UserOrgCom userOrgCom = UserOrgCom.builder().orgCom(orgCom).orgRole(Org.CHIEF).user(userService.findByTel("+0")).invitationStatus(InvitationStatus.ACCEPTED).build();
        userOrgComRepository.save(userOrgCom);
        Tournament tournament = Tournament.builder().sport(Sport.FOOTBALL).userOrgCom(userOrgCom).city(cityService.getCities().get(0)).minMembers(5).
                name("Tour1").id(UUID.fromString("91e4dfd6-4fc8-4523-9db6-33326263483c")).registerDeadline(LocalDate.of(2025, 01, 01)).is_stopped(false).build();
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

        ArrayList<Team> group2 = new ArrayList<>();
        group2.add(teamRepository.findByName("Team8").get());
        group2.add(teamRepository.findByName("Team9").get());


        Group groupFinal1 = Group.builder().teams(group1).name("A").stage(stage).build();
        Group groupFinal2 = Group.builder().teams(group2).name("B").stage(stage).build();
        groupRepository.save(groupFinal1);
        groupRepository.save(groupFinal2);
    }



    @Override
    public void run(String... args) throws Exception {
        cityService.loadCitiesFromJson();
        locationService.loadLocationsFromJson();
        createFirstUsers();
        createTeams(createOrgComAndChamp());
    }
}
