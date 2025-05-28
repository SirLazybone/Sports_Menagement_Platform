package com.course_work.Sports_Menagement_Platform.configuration;

import com.course_work.Sports_Menagement_Platform.data.enums.*;
import com.course_work.Sports_Menagement_Platform.data.models.*;
import com.course_work.Sports_Menagement_Platform.repositories.*;
import com.course_work.Sports_Menagement_Platform.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    
    @Autowired private UserRepository userRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private TournamentRepository tournamentRepository;
    @Autowired private OrgComRepository orgComRepository;
    @Autowired private UserOrgComRepository userOrgComRepository;
    @Autowired private UserTeamRepository userTeamRepository;
    @Autowired private TeamTournamentRepository teamTournamentRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private SlotRepository slotRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private MatchRepository matchRepository;
    @Autowired private GoalRepository goalRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private AfterMatchPenaltyRepository afterMatchPenaltyRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CityService cityService;
    @Autowired private LocationService locationService;

    private final Random random = new Random();
    
    // Массивы для генерации разнообразных данных
    private final String[] firstNames = {
        "Александр", "Дмитрий", "Максим", "Сергей", "Андрей", "Алексей", "Артём", "Илья", "Кирилл", "Михаил",
        "Никита", "Матвей", "Роман", "Егор", "Арсений", "Иван", "Денис", "Евгений", "Данил", "Тимур",
        "Анна", "Мария", "Елена", "Ольга", "Татьяна", "Наталья", "Ирина", "Светлана", "Екатерина", "Юлия"
    };
    
    private final String[] lastNames = {
        "Иванов", "Петров", "Сидоров", "Смирнов", "Кузнецов", "Попов", "Васильев", "Соколов", "Михайлов", "Новиков",
        "Фёдоров", "Морозов", "Волков", "Алексеев", "Лебедев", "Семёнов", "Егоров", "Павлов", "Козлов", "Степанов"
    };
    
    private final String[] teamNames = {
        "Динамо", "Спартак", "ЦСКА", "Зенит", "Локомотив", "Рубин", "Краснодар", "Ростов", "Урал", "Арсенал",
        "Торпедо", "Крылья Советов", "Ахмат", "Сочи", "Факел", "Оренбург", "Нижний Новгород", "Химки", "Балтика", "Пари НН",
        "Металлург", "Трактор", "Авангард", "СКА", "Салават Юлаев", "Ак Барс", "Витязь", "Северсталь", "Амур", "Барыс",
        "Енисей", "Кубань", "Алания", "Тамбов", "Волгарь", "Шинник", "Мордовия", "Луч", "Чайка", "Сибирь"
    };
    
    private final String[] tournamentNames = {
        "Кубок России", "Чемпионат города", "Летняя лига", "Зимний турнир", "Кубок дружбы", "Открытый чемпионат",
        "Молодёжная лига", "Ветеранский турнир", "Корпоративный кубок", "Студенческая лига", "Кубок федерации",
        "Первенство области", "Мемориал", "Суперкубок", "Кубок губернатора", "Турнир памяти", "Золотая осень",
        "Весенний кубок", "Новогодний турнир", "Майский кубок", "Турнир дружбы", "Кубок мира"
    };
    
    private final String[] orgComNames = {
        "Федерация футбола", "Баскетбольная ассоциация", "Волейбольный союз", "Хоккейная лига",
        "Спортивный комитет", "Организация турниров", "Лига чемпионов", "Спортивный центр",
        "Региональная федерация", "Областной спорткомитет", "Городская лига", "Молодёжная федерация"
    };

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            System.out.println("Запуск CommandLineAppStartupRunner...");
            
            // Простая проверка подключения к базе данных
            System.out.println("Проверка подключения к базе данных...");
            try {
                long userCount = userRepository.count();
                System.out.println("✅ Подключение к базе данных успешно. Найдено пользователей: " + userCount);
            } catch (Exception dbException) {
                System.err.println("❌ ОШИБКА подключения к базе данных:");
                System.err.println("Тип: " + dbException.getClass().getSimpleName());
                System.err.println("Сообщение: " + dbException.getMessage());
                dbException.printStackTrace();
                throw dbException;
            }
            
            // Инициализация базовых данных
            System.out.println("📍 Инициализация городов и локаций...");
            initializeCitiesAndLocations();
            
            // Создание синтетических данных только если база пуста
            System.out.println("👥 Проверка количества пользователей в базе...");
            long userCount = userRepository.count();
            System.out.println("Найдено пользователей: " + userCount);
            
            if (userCount == 0) {
                System.out.println("База данных пуста, создаём синтетические данные...");
                createSyntheticData();
            } else {
                System.out.println("База данных уже содержит данные, пропускаем создание синтетических данных");
            }

            createSpecialBasketballChampionship();
            
            System.out.println("CommandLineAppStartupRunner завершён успешно!");
            
        } catch (Exception e) {
            System.err.println("❌ ОШИБКА в CommandLineAppStartupRunner:");
            System.err.println("Тип ошибки: " + e.getClass().getSimpleName());
            System.err.println("Сообщение: " + e.getMessage());
            System.err.println("Полный стек ошибки:");
            e.printStackTrace();
            
            // Проверяем причину ошибки
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("\nПричина ошибки:");
                System.err.println("Тип: " + cause.getClass().getSimpleName());
                System.err.println("Сообщение: " + cause.getMessage());
                cause.printStackTrace();
            }
            
            throw e; // Перебрасываем ошибку дальше
        }
    }
    
    private void initializeCitiesAndLocations() {
        try {
            System.out.println("Проверка городов...");
            if (cityService.getCities() == null || cityService.getCities().isEmpty()) {
                System.out.println("Загрузка городов из JSON...");
                cityService.loadCitiesFromJson();
                System.out.println("Города загружены: " + (cityService.getCities() != null ? cityService.getCities().size() : 0));
            } else {
                System.out.println("Города уже загружены: " + cityService.getCities().size());
            }
            
            System.out.println("Проверка локаций...");
            if (locationService.getAllLocations() == null || locationService.getAllLocations().isEmpty()) {
                System.out.println("Загрузка локаций из JSON...");
                locationService.loadLocationsFromJson();
                System.out.println("Локации загружены: " + (locationService.getAllLocations() != null ? locationService.getAllLocations().size() : 0));
            } else {
                System.out.println("Локации уже загружены: " + locationService.getAllLocations().size());
            }
            
        } catch (Exception e) {
            System.err.println("❌ ОШИБКА при инициализации городов и локаций:");
            System.err.println("Тип ошибки: " + e.getClass().getSimpleName());
            System.err.println("Сообщение: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Не удалось инициализировать города и локации", e);
        }
    }
    
    @Transactional
    private void createSyntheticData() {
        System.out.println("Создание синтетических данных...");
        
        // 1. Создание пользователей (увеличено для обеспечения всех команд игроками)
        List<User> users = createUsers(200);
        
        // 2. Создание организационных комитетов
        List<OrgCom> orgComs = createOrgComs(4);
        
        // 3. Назначение пользователей в оргкомитеты
        assignUsersToOrgComs(users, orgComs);
        
        // 4. Создание турниров
        List<Tournament> tournaments = createTournaments(orgComs, 8);
        
        // 5. Создание команд (по 10 для каждого вида спорта = 40 команд)
        List<Team> teams = createTeams();
        
        // 6. Назначение игроков в команды
        assignPlayersToTeams(users, teams);
        
        // 7. Регистрация команд на турниры
        registerTeamsToTournaments(teams, tournaments);
        
        // 8. Создание локаций и слотов для турниров
        createLocationsAndSlots(tournaments);
        
        // 9. Создание этапов и групп
        List<Stage> stages = createStagesAndGroups(tournaments);
        
        // 10. Создание матчей
        List<Match> matches = createMatches(stages);
        
        // 11. Создание голов для матчей
        createGoals(matches);
        
        // 12. Создание штрафов после матчей
        createAfterMatchPenalties(matches);
        
        // 13. Создание приглашений (уменьшено количество)
        createInvitations(users, orgComs, teams);
        
        // 14. Создание специального баскетбольного чемпионата
        
        
        System.out.println("Синтетические данные созданы успешно!");
        System.out.println("=".repeat(50));
        System.out.println("📊 СТАТИСТИКА СОЗДАННЫХ ДАННЫХ:");
        System.out.println("=".repeat(50));
        System.out.println("👥 Пользователей: " + users.size() + " (1 админ + " + (users.size() - 1) + " обычных)");
        System.out.println("🏢 Организационных комитетов: " + orgComs.size());
        System.out.println("⚽ Команд: " + teams.size());
        
        // Разделяем турниры на активные и предстоящие для статистики
        List<Tournament> activeTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        List<Tournament> upcomingTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isAfter(LocalDate.now()) || 
                                    tournament.getRegisterDeadline().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
        
        System.out.println("🏆 Турниров всего: " + tournaments.size() + " (" + activeTournaments.size() + " активных + " + upcomingTournaments.size() + " предстоящих)");
        System.out.println("📅 Этапов: " + stages.size() + " (только для активных турниров)");
        System.out.println("⚔️ Матчей: " + matches.size());
        
        // Статистика по видам спорта
        System.out.println("\n🏃 СТАТИСТИКА ПО ВИДАМ СПОРТА:");
        for (Sport sport : Sport.values()) {
            long teamCount = teams.stream().filter(t -> t.getSport() == sport).count();
            long activeTournamentCount = activeTournaments.stream().filter(t -> t.getSport() == sport).count();
            long upcomingTournamentCount = upcomingTournaments.stream().filter(t -> t.getSport() == sport).count();
            System.out.println("  " + getSportNameInRussianNominative(sport).toUpperCase() + ": " + 
                             teamCount + " команд, " + activeTournamentCount + " активных + " + upcomingTournamentCount + " предстоящих турниров");
        }
        
        // Статистика по статусам
        System.out.println("\n📋 СТАТИСТИКА ПО СТАТУСАМ:");
        long stoppedTournaments = tournaments.stream().filter(t -> t.is_stopped()).count();
        System.out.println("  Остановленных турниров: " + stoppedTournaments);
        long publishedMatches = matches.stream().filter(Match::isResultPublished).count();
        System.out.println("  Матчей с опубликованными результатами: " + publishedMatches);
        
        System.out.println("\n🔮 ПРЕДСТОЯЩИЕ ТУРНИРЫ:");
        System.out.println("  📅 Всего предстоящих турниров: " + upcomingTournaments.size());
        System.out.println("  🎯 Команды НЕ зарегистрированы на предстоящие турниры");
        System.out.println("  📋 Этапы НЕ созданы для предстоящих турниров");
        
        System.out.println("\n✅ Данные готовы к использованию!");
        System.out.println("🔑 Админ: +79999999999 / admin");
        System.out.println("🔑 Пользователи: +7XXXXXXXXXX / password");
        System.out.println("=".repeat(50));
    }
    
    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        
        // Создание админа
        User admin = User.builder()
                .name("Администратор")
                .surname("Системы")
                .tel("+79999999999")
                .password(passwordEncoder.encode("admin"))
                .role(Role.ADMIN)
                .build();
        users.add(userRepository.save(admin));
        
        // Создание обычных пользователей
        for (int i = 1; i < count; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            
            User user = User.builder()
                    .name(firstName)
                    .surname(lastName)
                    .tel(String.format("+7%010d", 9000000000L + i))
                    .password(passwordEncoder.encode("password"))
                    .role(Role.USER)
                    .build();
            users.add(userRepository.save(user));
        }
        
        return users;
    }
    
    private List<OrgCom> createOrgComs(int count) {
        List<OrgCom> orgComs = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            OrgCom orgCom = OrgCom.builder()
                    .name(orgComNames[i % orgComNames.length] + " " + (i / orgComNames.length + 1))
                    .build();
            orgComs.add(orgComRepository.save(orgCom));
        }
        
        return orgComs;
    }
    
    private void assignUsersToOrgComs(List<User> users, List<OrgCom> orgComs) {
        int userIndex = 1; // Пропускаем админа
        
        for (OrgCom orgCom : orgComs) {
            // Назначаем главу оргкомитета
            if (userIndex < users.size()) {
                UserOrgCom chief = UserOrgCom.builder()
                        .user(users.get(userIndex++))
                        .orgCom(orgCom)
                        .orgRole(Org.CHIEF)
                        .isRef(random.nextBoolean())
                        .invitationStatus(InvitationStatus.ACCEPTED)
                        .build();
                userOrgComRepository.save(chief);
            }
            
            // Назначаем 2-5 организаторов
            int orgCount = 2 + random.nextInt(4);
            for (int i = 0; i < orgCount && userIndex < users.size(); i++) {
                UserOrgCom organizer = UserOrgCom.builder()
                        .user(users.get(userIndex++))
                        .orgCom(orgCom)
                        .orgRole(Org.ORG)
                        .isRef(random.nextBoolean())
                        .invitationStatus(InvitationStatus.ACCEPTED)
                        .build();
                userOrgComRepository.save(organizer);
            }
        }
    }
    
    private List<Tournament> createTournaments(List<OrgCom> orgComs, int count) {
        List<Tournament> tournaments = new ArrayList<>();
        List<City> cities = cityService.getCities();
        Sport[] sports = Sport.values();
        
        String[] descriptions = {
            "Престижный турнир с участием лучших команд региона",
            "Открытый чемпионат для всех желающих команд",
            "Традиционный турнир, проводящийся уже много лет",
            "Молодёжное первенство для развития спорта",
            "Корпоративный турнир между предприятиями",
            "Благотворительный турнир в поддержку спорта",
            "Международный турнир с участием зарубежных команд",
            "Региональный чемпионат высокого уровня",
            "Любительский турнир для начинающих команд",
            "Профессиональный турнир с денежными призами"
        };
        
        // Создаём ровно по 2 турнира для каждого вида спорта (активные турниры)
        int tournamentsPerSport = 2;
        
        for (int sportIndex = 0; sportIndex < sports.length; sportIndex++) {
            Sport sport = sports[sportIndex];
            
            System.out.println("🏆 Создаём " + tournamentsPerSport + " активных турниров для " + getSportNameInRussianNominative(sport));
            
            for (int i = 0; i < tournamentsPerSport; i++) {
                OrgCom orgCom = orgComs.get(random.nextInt(orgComs.size()));
                
                // Находим главу оргкомитета
                List<UserOrgCom> orgMembers = userOrgComRepository.findUsersByOrgComIdNotDTO(orgCom.getId());
                UserOrgCom organizer = orgMembers.stream()
                        .filter(uoc -> uoc.getOrgRole() == Org.CHIEF)
                        .findFirst()
                        .orElse(orgMembers.get(0));
                
                int minMembers = getMinMembersForSport(sport);
                
                String tournamentName = tournamentNames[random.nextInt(tournamentNames.length)];
                tournamentName += " по " + getSportNameInRussian(sport);
                
                Tournament tournament = Tournament.builder()
                        .name(tournamentName + " " + (2025))
                        .sport(sport)
                        .minMembers(minMembers)
                        .registerDeadline(LocalDate.now().minusDays(random.nextInt(30) + 1)) // Прошедшие даты для создания этапов
                        .description(descriptions[random.nextInt(descriptions.length)])
                        .is_stopped(false) // Все турниры активны
                        .userOrgCom(organizer)
                        .city(cities.get(random.nextInt(cities.size())))
                        .build();
                
                tournaments.add(tournamentRepository.save(tournament));
            }
        }
        
        // Создаём предстоящие турниры (без команд)
        int upcomingTournamentsPerSport = 3; // По 3 предстоящих турнира на каждый вид спорта
        
        System.out.println("\n🔮 Создаём предстоящие турниры...");
        
        for (int sportIndex = 0; sportIndex < sports.length; sportIndex++) {
            Sport sport = sports[sportIndex];
            
            System.out.println("🔮 Создаём " + upcomingTournamentsPerSport + " предстоящих турниров для " + getSportNameInRussianNominative(sport));
            
            for (int i = 0; i < upcomingTournamentsPerSport; i++) {
                OrgCom orgCom = orgComs.get(random.nextInt(orgComs.size()));
                
                // Находим главу оргкомитета
                List<UserOrgCom> orgMembers = userOrgComRepository.findUsersByOrgComIdNotDTO(orgCom.getId());
                UserOrgCom organizer = orgMembers.stream()
                        .filter(uoc -> uoc.getOrgRole() == Org.CHIEF)
                        .findFirst()
                        .orElse(orgMembers.get(0));
                
                int minMembers = getMinMembersForSport(sport);
                
                String tournamentName = tournamentNames[random.nextInt(tournamentNames.length)];
                tournamentName += " по " + getSportNameInRussian(sport);
                
                // Предстоящие турниры с будущими датами регистрации
                LocalDate futureDeadline = LocalDate.now().plusDays(random.nextInt(60) + 7); // От 7 до 67 дней в будущем
                
                Tournament upcomingTournament = Tournament.builder()
                        .name(tournamentName + " " + (2025 + random.nextInt(2))) // 2025 или 2026
                        .sport(sport)
                        .minMembers(minMembers)
                        .registerDeadline(futureDeadline)
                        .description(descriptions[random.nextInt(descriptions.length)])
                        .is_stopped(false)
                        .userOrgCom(organizer)
                        .city(cities.get(random.nextInt(cities.size())))
                        .build();
                
                tournaments.add(tournamentRepository.save(upcomingTournament));
                System.out.println("  ✅ Создан предстоящий турнир: " + upcomingTournament.getName() + 
                                 " (дедлайн: " + futureDeadline + ")");
            }
        }
        
        System.out.println("✅ Всего создано турниров: " + tournaments.size() + 
                         " (" + tournamentsPerSport + " активных + " + upcomingTournamentsPerSport + " предстоящих на каждый вид спорта)");
        return tournaments;
    }
    
    private String getSportNameInRussian(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> "футболу";
            case BASKETBALL -> "баскетболу";
            case VOLLEYBALL -> "волейболу";
            case HOCKEY -> "хоккею";
        };
    }
    
    private String getSportNameInRussianNominative(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> "футбол";
            case BASKETBALL -> "баскетбол";
            case VOLLEYBALL -> "волейбол";
            case HOCKEY -> "хоккей";
        };
    }
    
    private int getMinMembersForSport(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> 11;
            case BASKETBALL -> 5;
            case VOLLEYBALL -> 6;
            case HOCKEY -> 6;
        };
    }
    
    private List<Team> createTeams() {
        List<Team> teams = new ArrayList<>();
        Sport[] sports = Sport.values();
        List<City> cities = cityService.getCities();
        
        String[] teamPrefixes = {"ФК", "БК", "ВК", "ХК"}; // Футбольный, Баскетбольный, Волейбольный, Хоккейный клуб
        String[] teamSuffixes = {"Юниор", "Резерв", "Академия", "Спорт", "Плюс", "Про", "Элит", "Мастер"};
        
        // Создаём ровно по 10 команд для каждого вида спорта
        int teamsPerSport = 10;
        
        for (int sportIndex = 0; sportIndex < sports.length; sportIndex++) {
            Sport sport = sports[sportIndex];
            
            System.out.println("🏃 Создаём " + teamsPerSport + " команд для " + getSportNameInRussianNominative(sport));
            
            for (int i = 0; i < teamsPerSport; i++) {
                String baseName = teamNames[random.nextInt(teamNames.length)];
                
                // Добавляем префикс в зависимости от вида спорта
                String prefix = switch (sport) {
                    case FOOTBALL -> "ФК ";
                    case BASKETBALL -> "БК ";
                    case VOLLEYBALL -> "ВК ";
                    case HOCKEY -> "ХК ";
                };
                
                // Добавляем город (50% вероятность)
                String cityName = "";
                if (random.nextBoolean() && !cities.isEmpty()) {
                    cityName = cities.get(random.nextInt(cities.size())).getName() + " ";
                }
                
                // Добавляем суффикс (25% вероятность)
                String suffix = "";
                if (random.nextInt(4) == 0) {
                    suffix = " " + teamSuffixes[random.nextInt(teamSuffixes.length)];
                }
                
                String teamName = prefix + cityName + baseName + suffix;
                
                // Добавляем номер если команда с таким именем уже существует
                String finalName = teamName;
                int counter = 1;
                while (teamRepository.findByName(finalName).isPresent()) {
                    finalName = teamName + " " + counter++;
                }
                
                Team team = Team.builder()
                        .name(finalName)
                        .sport(sport)
                        .build();
                
                teams.add(teamRepository.save(team));
            }
        }
        
        System.out.println("✅ Всего создано команд: " + teams.size() + " (" + teamsPerSport + " на каждый вид спорта)");
        return teams;
    }
    
    private void assignPlayersToTeams(List<User> users, List<Team> teams) {
        int userIndex = 20; // Начинаем с 20-го пользователя, оставляя первых для оргкомитетов
        
        for (Team team : teams) {
            int minMembers = getMinMembersForSport(team.getSport());
            // Создаём команды с достаточным количеством игроков + небольшой запас
            int teamSize = minMembers + random.nextInt(3) + 2; // минимум + 2-4 дополнительных игрока
            
            boolean captainAssigned = false;
            
            for (int i = 0; i < teamSize && userIndex < users.size(); i++) {
                // ВСЕ игроки имеют статус ACCEPTED для гарантированного участия
                UserTeam userTeam = UserTeam.builder()
                        .user(users.get(userIndex++))
                        .team(team)
                        .isPlaying(random.nextInt(10) > 1) // 90% играющих
                        .isCap(!captainAssigned) // Первый игрок - капитан
                        .invitationStatus(InvitationStatus.ACCEPTED) // ВСЕ приняты
                        .build();
                
                userTeamRepository.save(userTeam);
                captainAssigned = true;
            }
            
            System.out.println("👥 Команда " + team.getName() + ": создано " + teamSize + " игроков (все приняты)");
        }
    }
    
    private int getMaxMembersForSport(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> 25;
            case BASKETBALL -> 15;
            case VOLLEYBALL -> 14;
            case HOCKEY -> 23;
        };
    }
    
    private InvitationStatus getRandomInvitationStatus() {
        int rand = random.nextInt(100);
        if (rand < 80) return InvitationStatus.ACCEPTED;
        if (rand < 90) return InvitationStatus.PENDING;
        if (rand < 95) return InvitationStatus.DECLINED;
        return InvitationStatus.LEFT;
    }
    
    private void registerTeamsToTournaments(List<Team> teams, List<Tournament> tournaments) {
        System.out.println("📊 Всего команд: " + teams.size() + ", турниров: " + tournaments.size());
        
        // Фильтруем турниры - регистрируем команды только на турниры с прошедшими датами регистрации
        List<Tournament> activeTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        
        List<Tournament> upcomingTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isAfter(LocalDate.now()) || 
                                    tournament.getRegisterDeadline().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
        
        System.out.println("🏆 Активных турниров (с прошедшими дедлайнами): " + activeTournaments.size());
        System.out.println("🔮 Предстоящих турниров (с будущими дедлайнами): " + upcomingTournaments.size());
        
        // Группируем команды и активные турниры по видам спорта
        Map<Sport, List<Team>> teamsBySport = new HashMap<>();
        Map<Sport, List<Tournament>> activeTournamentsBySport = new HashMap<>();
        
        // Группируем команды по видам спорта
        for (Sport sport : Sport.values()) {
            List<Team> suitableTeams = teams.stream()
                    .filter(team -> team.getSport() == sport)
                    .filter(team -> {
                        int teamSize = getTeamSize(team);
                        int minRequired = getMinMembersForSport(sport);
                        boolean suitable = teamSize >= minRequired;
                        if (!suitable) {
                            System.out.println("  ❌ Команда " + team.getName() + " (" + sport + "): " + teamSize + " игроков, минимум " + minRequired);
                        }
                        return suitable;
                    })
                    .collect(Collectors.toList());
            teamsBySport.put(sport, suitableTeams);
            System.out.println("📋 " + getSportNameInRussianNominative(sport) + ": " + suitableTeams.size() + " подходящих команд из 10");
        }
        
        // Группируем ТОЛЬКО активные турниры по видам спорта
        for (Sport sport : Sport.values()) {
            List<Tournament> sportTournaments = activeTournaments.stream()
                    .filter(tournament -> tournament.getSport() == sport)
                    .collect(Collectors.toList());
            activeTournamentsBySport.put(sport, sportTournaments);
            System.out.println("🏆 " + getSportNameInRussianNominative(sport) + ": " + sportTournaments.size() + " активных турниров");
        }
        
        // Регистрируем команды ТОЛЬКО на активные турниры по видам спорта
        for (Sport sport : Sport.values()) {
            List<Team> sportTeams = teamsBySport.get(sport);
            List<Tournament> sportTournaments = activeTournamentsBySport.get(sport);
            
            if (sportTeams.isEmpty() || sportTournaments.isEmpty()) {
                System.out.println("⚠️ Пропускаем " + getSportNameInRussianNominative(sport) + " - нет команд или активных турниров");
                continue;
            }
            
            System.out.println("\n🔗 Регистрируем команды " + getSportNameInRussianNominative(sport).toUpperCase() + " ТОЛЬКО на активные турниры");
            
            // Регистрируем ВСЕ подходящие команды на ВСЕ активные турниры данного вида спорта
            for (Tournament tournament : sportTournaments) {
                System.out.println("  🏆 Активный турнир: " + tournament.getName() + " (дедлайн: " + tournament.getRegisterDeadline() + ")");
                
                int registeredCount = 0;
                
                // Регистрируем все подходящие команды
                for (Team team : sportTeams) {
                    // Проверяем, что команда ещё не зарегистрирована на этот турнир
                    boolean alreadyRegistered = teamTournamentRepository
                            .findByTournamentIdAndTeamId(tournament.getId(), team.getId())
                            .isPresent();
                    
                    if (!alreadyRegistered) {
                        // Все команды принимаются для гарантированного создания этапов
                        TeamTournament teamTournament = TeamTournament.builder()
                                .team(team)
                                .tournament(tournament)
                                .applicationStatus(ApplicationStatus.ACCEPTED)
                                .goToPlayOff(random.nextBoolean())
                                .build();
                        
                        teamTournamentRepository.save(teamTournament);
                        registeredCount++;
                        System.out.println("    ✅ " + team.getName() + " → ACCEPTED");
                    } else {
                        System.out.println("    ⚠️ " + team.getName() + " уже зарегистрирована");
                    }
                }
                
                System.out.println("    📊 Зарегистрировано " + registeredCount + " команд на активный турнир");
            }
        }
        
        // Выводим информацию о предстоящих турнирах (они остаются пустыми)
        System.out.println("\n🔮 ПРЕДСТОЯЩИЕ ТУРНИРЫ (остаются без команд):");
        for (Sport sport : Sport.values()) {
            List<Tournament> sportUpcomingTournaments = upcomingTournaments.stream()
                    .filter(tournament -> tournament.getSport() == sport)
                    .collect(Collectors.toList());
            
            if (!sportUpcomingTournaments.isEmpty()) {
                System.out.println("  " + getSportNameInRussianNominative(sport).toUpperCase() + ":");
                for (Tournament tournament : sportUpcomingTournaments) {
                    System.out.println("    🔮 " + tournament.getName() + " (дедлайн: " + tournament.getRegisterDeadline() + ")");
                }
            }
        }
        
        System.out.println("\n✅ Регистрация команд завершена");
        System.out.println("📊 Команды зарегистрированы только на " + activeTournaments.size() + " активных турнирах");
        System.out.println("🔮 " + upcomingTournaments.size() + " предстоящих турниров остались пустыми");
    }
    
    private int getTeamSize(Team team) {
        List<User> allUsers = userTeamRepository.findAllUserByTeam(team.getId());
        List<UserTeam> userTeams = allUsers.stream()
                .map(user -> userTeamRepository.findByUser_IdAndTeam_Id(user.getId(), team.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(ut -> ut.getInvitationStatus() == InvitationStatus.ACCEPTED)
                .toList();
        
        int teamSize = userTeams.size();
        if (teamSize == 0) {
            System.out.println("    🔍 Команда " + team.getName() + ": всего пользователей " + allUsers.size() + ", принятых " + teamSize);
        }
        return teamSize;
    }
    
    private ApplicationStatus getRandomApplicationStatus() {
        int rand = random.nextInt(100);
        if (rand < 70) return ApplicationStatus.ACCEPTED;
        if (rand < 85) return ApplicationStatus.PENDING;
        if (rand < 95) return ApplicationStatus.DECLINED;
        return ApplicationStatus.CANCELED;
    }
    
    private void createLocationsAndSlots(List<Tournament> tournaments) {
        String[] locationNames = {
            "Центральный стадион", "Спортивный комплекс", "Арена", "Дворец спорта"
        };
        
        String[] addresses = {
            "ул. Ленина, 1", "пр. Мира, 15", "ул. Спортивная, 3", "пл. Победы, 7"
        };
        
        for (Tournament tournament : tournaments) {
            // Создаем только 1 локацию для каждого турнира (уменьшено с 1-3)
            Location location = Location.builder()
                    .name(locationNames[random.nextInt(locationNames.length)])
                    .address(addresses[random.nextInt(addresses.length)])
                    .tournament(tournament)
                    .build();
            
            location = locationRepository.save(location);
            
            // Создаем слоты для локации
            createSlotsForLocation(location, tournament);
        }
    }
    
    private void createSlotsForLocation(Location location, Tournament tournament) {
        LocalDate startDate = LocalDate.now().plusDays(1); // Начинаем с завтрашнего дня
        LocalDate endDate = startDate.plusDays(14); // Уменьшено с 30 до 14 дней
        
        LocalTime[] timeSlots = {
            LocalTime.of(10, 0), LocalTime.of(14, 0), LocalTime.of(18, 0) // Уменьшено количество слотов
        };
        
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            // Создаём слоты только через день
            if (random.nextBoolean()) continue;
            
            int slotsPerDay = 1 + random.nextInt(2); // Максимум 2 слота в день
            List<LocalTime> daySlots = new ArrayList<>(Arrays.asList(timeSlots));
            Collections.shuffle(daySlots);
            
            for (int i = 0; i < slotsPerDay; i++) {
                Slot slot = Slot.builder()
                        .date(date)
                        .time(daySlots.get(i))
                        .location(location)
                        .build();
                
                slotRepository.save(slot);
            }
        }
    }
    
    private List<Stage> createStagesAndGroups(List<Tournament> tournaments) {
        List<Stage> stages = new ArrayList<>();
        
        // Фильтруем турниры - создаём этапы только для турниров с прошедшими датами регистрации
        List<Tournament> activeTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isBefore(LocalDate.now()))
                .collect(Collectors.toList());
        
        List<Tournament> upcomingTournaments = tournaments.stream()
                .filter(tournament -> tournament.getRegisterDeadline().isAfter(LocalDate.now()) || 
                                    tournament.getRegisterDeadline().isEqual(LocalDate.now()))
                .collect(Collectors.toList());
        
        System.out.println("🎯 Начинаем создание этапов для " + activeTournaments.size() + " активных турниров");
        System.out.println("🔮 Пропускаем " + upcomingTournaments.size() + " предстоящих турниров (этапы не создаются)");
        
        for (Tournament tournament : activeTournaments) {
            System.out.println("\n--- Обрабатываем активный турнир: " + tournament.getName() + " (ID: " + tournament.getId() + ") ---");
            
            List<TeamTournament> acceptedTeams = teamTournamentRepository
                    .findAllTeamsByTournamentIdAndStatus(tournament.getId(), ApplicationStatus.ACCEPTED)
                    .stream()
                    .map(team -> teamTournamentRepository.findByTournamentIdAndTeamId(tournament.getId(), team.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
            
            System.out.println("🎯 Турнир '" + tournament.getName() + "' имеет " + acceptedTeams.size() + " принятых команд");
            
            // Дополнительная отладка - проверим все команды турнира
            List<TeamTournament> allTeams = teamTournamentRepository.findAllTeamTournamentByTournamentId(tournament.getId());
            System.out.println("📊 Всего команд в турнире: " + allTeams.size());
            for (TeamTournament tt : allTeams) {
                System.out.println("  - Команда: " + tt.getTeam().getName() + ", Статус: " + tt.getApplicationStatus());
            }
            
            if (acceptedTeams.size() < 4) {
                System.out.println("❌ Недостаточно команд для создания этапов в турнире: " + tournament.getName());
                continue;
            }
            
            // Создаем групповой этап для всех турниров
            System.out.println("✅ Создаём групповой этап для турнира: " + tournament.getName());
            Stage groupStage = Stage.builder()
                    .tournament(tournament)
                    .isPublished(true) // Всегда публикуем этапы
                    .bestPlace(0)
                    .worstPlace(0)
                    .build();
            
            groupStage = stageRepository.save(groupStage);
            stages.add(groupStage);
            System.out.println("✅ Групповой этап создан с ID: " + groupStage.getId());
            
            // Создаем группы
            System.out.println("📋 Создаём группы для этапа...");
            createGroupsForStage(groupStage, acceptedTeams);
            
            // Создаем плей-офф этап для турниров с 8+ командами
            if (acceptedTeams.size() >= 8) {
                System.out.println("🏆 Создаём плей-офф этап (команд >= 8)");
                Stage playoffStage = Stage.builder()
                        .tournament(tournament)
                        .isPublished(true) // Всегда публикуем этапы
                        .bestPlace(1)
                        .worstPlace(Math.min(8, acceptedTeams.size()))
                        .build();
                
                Stage savedPlayoffStage = stageRepository.save(playoffStage);
                stages.add(savedPlayoffStage);
                System.out.println("✅ Создан плей-офф этап для турнира: " + tournament.getName() + " с ID: " + savedPlayoffStage.getId());
            } else {
                System.out.println("ℹ️ Плей-офф этап не создан (команд < 8): " + acceptedTeams.size());
            }
            
            System.out.println("✅ Групповой этап создан для турнира: " + tournament.getName());
        }
        
        // Выводим информацию о предстоящих турнирах
        if (!upcomingTournaments.isEmpty()) {
            System.out.println("\n🔮 ПРЕДСТОЯЩИЕ ТУРНИРЫ (этапы не создаются):");
            for (Tournament tournament : upcomingTournaments) {
                System.out.println("  🔮 " + tournament.getName() + " (дедлайн: " + tournament.getRegisterDeadline() + ")");
            }
        }
        
        System.out.println("\n🎯 Всего создано этапов: " + stages.size() + " (только для активных турниров)");
        return stages;
    }
    
    private void createGroupsForStage(Stage stage, List<TeamTournament> teams) {
        int teamCount = teams.size();
        int groupCount = Math.max(1, teamCount / 5); // Примерно 5 команд в группе
        
        System.out.println("📋 Создание групп: " + teamCount + " команд, " + groupCount + " групп");
        
        String[] groupNames = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
        List<List<Team>> groups = new ArrayList<>();
        for (int i = 0; i < groupCount; i++) {
            groups.add(new ArrayList<>());
        }
        
        // Распределяем команды по группам
        for (int i = 0; i < teams.size(); i++) {
            groups.get(i % groupCount).add(teams.get(i).getTeam());
            System.out.println("  Команда " + teams.get(i).getTeam().getName() + " → Группа " + groupNames[i % groupCount]);
        }
        
        // Создаем группы в базе данных
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).isEmpty()) {
                Group group = Group.builder()
                        .name(groupNames[i % groupNames.length])
                        .stage(stage)
                        .teams(groups.get(i))
                        .build();
                
                Group savedGroup = groupRepository.save(group);
                System.out.println("✅ Создана группа " + savedGroup.getName() + " с " + savedGroup.getTeams().size() + " командами (ID: " + savedGroup.getId() + ")");
            }
        }
    }
    
    private List<Match> createMatches(List<Stage> stages) {
        List<Match> matches = new ArrayList<>();
        List<Slot> allSlots = slotRepository.findAll();
        int slotIndex = 0;
        
        for (Stage stage : stages) {
            List<Group> groups = groupRepository.findByStageId(stage.getId());
            System.out.println("⚔️ Создаём матчи для этапа турнира '" + stage.getTournament().getName() + "' с " + groups.size() + " группами");
            
            for (Group group : groups) {
                List<Team> teams = group.getTeams();
                System.out.println("  📋 Группа " + group.getName() + " содержит " + teams.size() + " команд");
                
                if (teams.size() < 2) {
                    System.out.println("  ⚠️ Недостаточно команд в группе " + group.getName());
                    continue;
                }
                
                // Создаем матчи между всеми командами в группе (круговая система)
                int matchesCreated = 0;
                for (int i = 0; i < teams.size(); i++) {
                    for (int j = i + 1; j < teams.size(); j++) {
                        // Находим доступный слот для матча
                        Slot slot = null;
                        if (!allSlots.isEmpty()) {
                            slot = allSlots.get(slotIndex % allSlots.size());
                            slotIndex++;
                        }
                        
                        if (slot != null) {
                            Match match = Match.builder()
                                    .stage(stage)
                                    .team1(teams.get(i))
                                    .team2(teams.get(j))
                                    .slot(slot)
                                    .isResultPublished(true) // Всегда публикуем результаты для создания голов
                                    .build();
                            
                            matches.add(matchRepository.save(match));
                            matchesCreated++;
                        } else {
                            System.out.println("  ⚠️ Нет доступных слотов для матча");
                        }
                    }
                }
                System.out.println("  ✅ Создано " + matchesCreated + " матчей для группы " + group.getName());
            }
        }
        
        System.out.println("🎮 Всего создано матчей: " + matches.size());
        return matches;
    }
    
    private void createGoals(List<Match> matches) {
        for (Match match : matches) {
            // Создаем голы только для опубликованных результатов
            if (!match.isResultPublished()) continue;
            
            Sport sport = match.getTeam1().getSport();
            
            // Получаем игроков команд через UserTeam
            List<User> team1Players = userTeamRepository.findAllUserByTeam(match.getTeam1().getId());
            List<User> team2Players = userTeamRepository.findAllUserByTeam(match.getTeam2().getId());
            
            if (team1Players.isEmpty() || team2Players.isEmpty()) continue;
            
            // Уменьшаем количество голов
            int maxGoals = Math.min(3, getMaxGoalsForSport(sport)); // Максимум 3 гола
            int team1Goals = random.nextInt(maxGoals) + 1;
            int team2Goals = random.nextInt(maxGoals) + 1;
            
            // Создаем голы для первой команды
            for (int i = 0; i < team1Goals; i++) {
                User scorer = team1Players.get(random.nextInt(team1Players.size()));
                Goal goal = createGoalForSport(sport, match, match.getTeam1(), scorer);
                goalRepository.save(goal);
            }
            
            // Создаем голы для второй команды
            for (int i = 0; i < team2Goals; i++) {
                User scorer = team2Players.get(random.nextInt(team2Players.size()));
                Goal goal = createGoalForSport(sport, match, match.getTeam2(), scorer);
                goalRepository.save(goal);
            }
        }
    }
    
    private Goal createGoalForSport(Sport sport, Match match, Team team, User player) {
        Goal.GoalBuilder goalBuilder = Goal.builder()
                .match(match)
                .team(team)
                .player(player);
        
        switch (sport) {
            case FOOTBALL, HOCKEY -> {
                goalBuilder.time(random.nextInt(90) + 1)
                          .isPenalty(random.nextInt(10) == 0); // 10% пенальти
            }
            case BASKETBALL -> {
                goalBuilder.time(random.nextInt(48) + 1)
                          .points(random.nextBoolean() ? 2 : 3); // 2 или 3 очка
            }
            case VOLLEYBALL -> {
                goalBuilder.set_number(random.nextInt(5) + 1)
                          .points(1); // В волейболе всегда 1 очко
            }
        }
        
        return goalBuilder.build();
    }
    
    private int getMaxGoalsForSport(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> 5;
            case BASKETBALL -> 15;
            case VOLLEYBALL -> 25;
            case HOCKEY -> 6;
        };
    }
    
    private void createAfterMatchPenalties(List<Match> matches) {
        for (Match match : matches) {
            // Создаем штрафы только для некоторых матчей (ещё реже)
            if (!match.isResultPublished() || random.nextInt(5) != 0) continue; // Только 20% матчей
            
            // Получаем игроков команд через UserTeam
            List<User> team1Players = userTeamRepository.findAllUserByTeam(match.getTeam1().getId());
            List<User> team2Players = userTeamRepository.findAllUserByTeam(match.getTeam2().getId());
            
            if (team1Players.isEmpty() || team2Players.isEmpty()) continue;
            
            // Создаем максимум 1-2 штрафа для каждой команды
            int team1Penalties = random.nextInt(2) + 1;
            int team2Penalties = random.nextInt(2) + 1;
            
            // Создаем штрафы для первой команды
            for (int i = 0; i < team1Penalties; i++) {
                User penaltyTaker = team1Players.get(random.nextInt(team1Players.size()));
                AfterMatchPenalty penalty = AfterMatchPenalty.builder()
                        .match(match)
                        .team(match.getTeam1())
                        .player(penaltyTaker)
                        .scored(random.nextBoolean()) // 50% забитых штрафных
                        .build();
                afterMatchPenaltyRepository.save(penalty);
            }
            
            // Создаем штрафы для второй команды
            for (int i = 0; i < team2Penalties; i++) {
                User penaltyTaker = team2Players.get(random.nextInt(team2Players.size()));
                AfterMatchPenalty penalty = AfterMatchPenalty.builder()
                        .match(match)
                        .team(match.getTeam2())
                        .player(penaltyTaker)
                        .scored(random.nextBoolean()) // 50% забитых штрафных
                        .build();
                afterMatchPenaltyRepository.save(penalty);
            }
        }
    }
    
    private AfterMatchPenalty createPenaltyForSport(Sport sport, Match match, Team team, User player) {
        return AfterMatchPenalty.builder()
                .match(match)
                .team(team)
                .player(player)
                .scored(random.nextBoolean())
                .build();
    }
    
    private int getMaxPenaltiesForSport(Sport sport) {
        return switch (sport) {
            case FOOTBALL -> 3;
            case BASKETBALL -> 5;
            case VOLLEYBALL -> 2;
            case HOCKEY -> 4;
        };
    }
    
    private void createInvitations(List<User> users, List<OrgCom> orgComs, List<Team> teams) {
        // Создаем приглашения в оргкомитеты (только для некоторых пользователей)
        // Уменьшено с 50 до 15 пользователей
        for (int i = 0; i < Math.min(15, users.size()); i++) {
            User user = users.get(i);
            OrgCom orgCom = orgComs.get(random.nextInt(orgComs.size()));
            
            // Проверяем, что пользователь ещё не в этом оргкомитете
            boolean alreadyInOrgCom = userOrgComRepository.findUsersByOrgComIdNotDTO(orgCom.getId())
                    .stream()
                    .anyMatch(u -> u.getId().equals(user.getId()));
            
            if (!alreadyInOrgCom) {
                Invitation invitation = new Invitation();
                invitation.setInviter(users.get(0)); // Админ как инвайтер
                invitation.setInvitee(user);
                invitation.setOrgCom(orgCom);
                invitation.setStatus(getRandomInvitationStatus());
                invitation.setOrgRole(random.nextBoolean() ? Org.ORG : Org.CHIEF);
                
                invitationRepository.save(invitation);
            }
        }
    }
    
    private void createSpecialBasketballChampionship() {
        System.out.println("🏀 Создание специального баскетбольного чемпионата...");
        
        // Найдем оргкомитет "Баскетбольная ассоциация 1"
        OrgCom basketballOrgCom = orgComRepository.findAll().stream()
                .filter(org -> org.getName().contains("Баскетбольная ассоциация"))
                .findFirst()
                .orElse(orgComRepository.findAll().stream().findFirst().orElse(null)); // Если не найден, берем первый
        
        if (basketballOrgCom == null) {
            System.out.println("❌ Не найдено ни одного оргкомитета!");
            return;
        }
        
        System.out.println("📋 Найден оргкомитет: " + basketballOrgCom.getName());
        
        // Находим организатора из этого оргкомитета
        List<UserOrgCom> orgMembers = userOrgComRepository.findUsersByOrgComIdNotDTO(basketballOrgCom.getId());
        UserOrgCom organizer = orgMembers.stream()
                .filter(uoc -> uoc.getOrgRole() == Org.CHIEF)
                .findFirst()
                .orElse(orgMembers.get(0));
        
        // Находим случайный город
        List<City> cities = cityService.getCities();
        City randomCity = cities.get(random.nextInt(cities.size()));
        
        // Создаем турнир с будущим дедлайном регистрации
        LocalDate futureDeadline = LocalDate.now().plusDays(15); // 15 дней в будущем
        
        Tournament tournament = Tournament.builder()
                .name("Специальный чемпионат по баскетболу 2025")
                .sport(Sport.BASKETBALL)
                .minMembers(5) // Минимум для баскетбола
                .registerDeadline(futureDeadline)
                .description("Специальный чемпионат по баскетболу с групповым этапом. Регистрация команд открыта до " + futureDeadline)
                .is_stopped(false)
                .userOrgCom(organizer)
                .city(randomCity)
                .build();
        
        tournament = tournamentRepository.save(tournament);
        System.out.println("🏆 Создан турнир: " + tournament.getName() + " (ID: " + tournament.getId() + ")");
        System.out.println("📅 Дедлайн регистрации: " + futureDeadline);
        
        // Находим все баскетбольные команды
        List<Team> basketballTeams = teamRepository.findAll().stream()
                .filter(team -> team.getSport() == Sport.BASKETBALL)
                .limit(10) // Берем все 10 команд по баскетболу
                .collect(Collectors.toList());
        
        System.out.println("🏀 Найдено баскетбольных команд: " + basketballTeams.size());
        
        // Регистрируем все команды на турнир (все команды приняты)
        for (Team team : basketballTeams) {
            TeamTournament teamTournament = TeamTournament.builder()
                    .team(team)
                    .tournament(tournament)
                    .applicationStatus(ApplicationStatus.ACCEPTED) // Все команды приняты
                    .goToPlayOff(false) // Пока не определено
                    .build();
            
            teamTournamentRepository.save(teamTournament);
            System.out.println("  ✅ Команда " + team.getName() + " зарегистрирована (ACCEPTED)");
        }
        
        // Создаем НЕОПУБЛИКОВАННЫЙ групповой этап
        Stage groupStage = Stage.builder()
                .tournament(tournament)
                .isPublished(false) // Этап НЕ опубликован
                .bestPlace(0)
                .worstPlace(0)
                .build();
        
        groupStage = stageRepository.save(groupStage);
        System.out.println("📋 Создан групповой этап (НЕОПУБЛИКОВАННЫЙ) с ID: " + groupStage.getId());
        
        // Создаем одну группу A с командами
        Group group = Group.builder()
                .name("A")
                .stage(groupStage)
                .teams(basketballTeams)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        System.out.println("👥 Создана группа A с " + basketballTeams.size() + " командами (ID: " + savedGroup.getId() + ")");
        
        System.out.println("🎯 ИТОГ:");
        System.out.println("  🏆 Турнир: " + tournament.getName());
        System.out.println("  🏢 Оргкомитет: " + basketballOrgCom.getName());
        System.out.println("  📅 Дедлайн: " + futureDeadline + " (ещё не прошёл)");
        System.out.println("  👥 Команд: " + basketballTeams.size() + " (все ACCEPTED)");
        System.out.println("  📋 Групповой этап: создан, НЕ опубликован");
        System.out.println("  🎮 Матчи: НЕ созданы (этап не опубликован)");
        System.out.println("✅ Специальный баскетбольный чемпионат готов!");
    }
}
