package de.fsr.mariokart_backend;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsCreateService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.survey.model.QuestionType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyCreateService;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class MarioKartStartupRunner implements CommandLineRunner {
    private final PublicRegistrationCreateService publicRegistrationCreateService;
    private final AdminScheduleCreateService adminScheduleCreateService;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final AddCharacterService addCharacterService;
    private final UserService userService;
    private final AdminSettingsCreateService adminSettingsCreateService;
    private final AdminSettingsUpdateService adminSettingsUpdateService;
    private final AdminSurveyCreateService adminSurveyCreateService;

    @Override
    public void run(String... args) throws Exception {
        try {
            addCharacterService.addCharacters("media");
            adminSettingsCreateService.createSettings();
        } catch (IllegalStateException | IOException e) {
            System.err.print(e.getMessage());
        }

        addUser();
        addTeams();
        // addSurvey();
        // addRounds();
        // addGames();

    }

    private void addSurvey() {
        try {
            adminSurveyCreateService.createQuestion(new QuestionInputDTO("Wie zufrieden bist du mit dem Turnier?",
                    QuestionType.MULTIPLE_CHOICE.toString(),
                    List.of("Sehr zufrieden", "Zufrieden", "Neutral", "Unzufrieden", "Sehr unzufrieden"),
                    true,
                    true, false, false));
            adminSurveyCreateService.createQuestion(new QuestionInputDTO(("Was würdest du verbessern?"),
                    QuestionType.FREE_TEXT.toString(),
                    null,
                    true,
                    true, false, false));

            adminSurveyCreateService.createQuestion(new QuestionInputDTO("Was sind deine Lieblingscharaktere?",
                    QuestionType.CHECKBOX.toString(),
                    List.of("Mario", "Luigi", "Peach", "Bowser", "Toad", "Yoshi", "Donkey-Kong", "Wario", "Waluigi",
                            "Daisy", "Rosalina", "Metall-Mario", "Shy-Guy", "Knochentrocken", "Lakitu", "König-Buu-Huu",
                            "Koopa", "Inkling-Mädchen", "Bewohner", "Baby-Daisy", "Melinda"),
                    true,
                    true, false, false));
            adminSurveyCreateService.createQuestion(new QuestionInputDTO("Wähle dein Lieblingsteam aus",
                    QuestionType.TEAM.toString(),
                    null,
                    false,
                    false, false, false));
            adminSurveyCreateService.createQuestion(new QuestionInputDTO("Welches Team wird das Finale gewinnen?",
                    QuestionType.TEAM.toString(),
                    null,
                    false,
                    false, false, true));

        } catch (IllegalArgumentException e) {
            System.err.print(e.getMessage());
        }

    }

    private void addUser() {
        if (userService.getUsers().isEmpty()) {
            try {
                if (userService.getUser(System.getenv("USER_NAME")) != null) {
                    System.err.print("User already exists!");
                    return;
                }
            } catch (EntityNotFoundException e) {
                System.err.print(e.getMessage());
            }
            User user = new User(System.getenv("USER_NAME"), true);
            user.setPassword(System.getenv("USER_PASSWORD"));
            userService.createAndRegisterIfNotExist(user);
        }

    }

    private void addTeams() {
        try {
            adminSettingsUpdateService.updateSettings(new TournamentDTO(true, true, 6));
            TeamInputDTO team1 = new TeamInputDTO("TollerTeamName", "Mario");
            publicRegistrationCreateService.registerTeam(team1);

            TeamInputDTO team2 = new TeamInputDTO("BlitzBoys", "Luigi");
            publicRegistrationCreateService.registerTeam(team2);

            TeamInputDTO team3 = new TeamInputDTO("ToadstoolTerrors", "Peach");
            publicRegistrationCreateService.registerTeam(team3);

            TeamInputDTO team4 = new TeamInputDTO("KoopaKings", "Bowser");
            publicRegistrationCreateService.registerTeam(team4);

            TeamInputDTO team5 = new TeamInputDTO("MushroomMasters", "Toad");
            publicRegistrationCreateService.registerTeam(team5);

            TeamInputDTO team6 = new TeamInputDTO("BulletBillBrigade", "Yoshi");
            publicRegistrationCreateService.registerTeam(team6);

            TeamInputDTO team7 = new TeamInputDTO("ChompChampions", "Donkey-Kong");
            publicRegistrationCreateService.registerTeam(team7);

            TeamInputDTO team8 = new TeamInputDTO("RainbowRiders", "Wario");
            publicRegistrationCreateService.registerTeam(team8);

            TeamInputDTO team9 = new TeamInputDTO("ShellShockers", "Waluigi");
            publicRegistrationCreateService.registerTeam(team9);

            TeamInputDTO team10 = new TeamInputDTO("BananaBandits", "Daisy");
            publicRegistrationCreateService.registerTeam(team10);

            TeamInputDTO team11 = new TeamInputDTO("PiranhaPals", "Rosalina");
            publicRegistrationCreateService.registerTeam(team11);

            TeamInputDTO team12 = new TeamInputDTO("ThwompThumpers", "Metall-Mario");
            publicRegistrationCreateService.registerTeam(team12);

            TeamInputDTO team13 = new TeamInputDTO("ShyGuySquad", "Shy-Guy");
            publicRegistrationCreateService.registerTeam(team13);

            TeamInputDTO team14 = new TeamInputDTO("DryBoneDynasty", "Knochentrocken");
            publicRegistrationCreateService.registerTeam(team14);

            TeamInputDTO team15 = new TeamInputDTO("LakituLegends", "Lakitu");
            publicRegistrationCreateService.registerTeam(team15);

            TeamInputDTO team16 = new TeamInputDTO("BooBusters", "König-Buu-Huu");
            publicRegistrationCreateService.registerTeam(team16);

            TeamInputDTO team17 = new TeamInputDTO("KoopaTroop", "Koopa");
            publicRegistrationCreateService.registerTeam(team17);

            TeamInputDTO team18 = new TeamInputDTO("InklingInvaders", "Inkling-Mädchen");
            publicRegistrationCreateService.registerTeam(team18);

            TeamInputDTO team19 = new TeamInputDTO("VillagerVictory", "Bewohner");
            publicRegistrationCreateService.registerTeam(team19);

            TeamInputDTO team20 = new TeamInputDTO("BabyBruisers", "Baby-Daisy");
            publicRegistrationCreateService.registerTeam(team20);

            TeamInputDTO team21 = new TeamInputDTO("Isabelle'sIsle", "Melinda");
            publicRegistrationCreateService.registerTeam(team21);
        } catch (IllegalArgumentException | EntityNotFoundException | RoundsAlreadyExistsException e) {
            System.err.print(e.getMessage());
        }

    }

    private void addRounds() {
        RoundInputDTO round1 = new RoundInputDTO(false);
        adminScheduleCreateService.addRound(round1);

        RoundInputDTO round2 = new RoundInputDTO(false);
        adminScheduleCreateService.addRound(round2);

        RoundInputDTO round3 = new RoundInputDTO(false);
        adminScheduleCreateService.addRound(round3);

        List<Round> rounds = roundRepository.findAll();
        for (int i = 0; i < rounds.size(); i++) {
            rounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(20L * i));
            rounds.get(i).setEndTime(LocalDateTime.now().plusMinutes(20L * i).plusMinutes(20L));
            roundRepository.save(rounds.get(i));
        }
    }

    private void addGames() throws EntityNotFoundException {
        List<Round> rounds = roundRepository.findAll();
        List<Team> teams = teamRepository.findAll();
        for (Round round : rounds) {
            for (int i = 0; i < 4; i++) {
                Game game = new Game();
                if (i == 0) {
                    game.setSwitchGame("Blau");
                } else if (i == 1) {
                    game.setSwitchGame("Rot");
                } else if (i == 2) {
                    game.setSwitchGame("Grün");
                } else {
                    game.setSwitchGame("Weiß");
                }
                game.setRound(round);
                adminScheduleCreateService.addGame(game);

                Collections.shuffle(teams);

                List<Team> selectedTeams = teams.subList(0, 4);

                for (Team team : selectedTeams) {
                    Points point = new Points();
                    point.setGroupPoints(0);
                    point.setFinalPoints(0);
                    point.setTeam(team);
                    point.setGame(game);
                    adminScheduleCreateService.addPoints(point);
                }

            }
        }
    }
}
