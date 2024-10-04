package de.fsr.mariokart_backend;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanService;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.repository.UserRepository;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.service.RegistrationService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class MarioKartStartupRunner implements CommandLineRunner {
    private final RegistrationService registrationService;
    private final MatchPlanService matchPlanService;
    private final RoundRepository roundRepository;
    private final TeamRepository teamRepository;
    private final AddCharacterService addCharacterService;
    private final UserService userService;
    private final SettingsService settingsService;

    @Override
    public void run(String... args) throws Exception {
        try{
            addCharacterService.addCharacters("media");
            settingsService.createSettings();
        } catch (IllegalStateException | IOException e) {
            System.err.print(e.getMessage());
        }


        addUser();
        addTeams();
//        addRounds();
//        addGames();

    }

    private void addUser(){
        if (userService.getUsers().isEmpty()) {
            User user = new User("FSR", true);
            user.setPassword("Passwort1234");
            userService.createAndRegisterIfNotExist(user);
        }
    }

    private void addTeams() {
        try {
            settingsService.updateSettings(new TournamentDTO(true, true));
            TeamInputDTO team1 = new TeamInputDTO("TollerTeamName", "Mario");
            registrationService.addTeam(team1);

            TeamInputDTO team2 = new TeamInputDTO("BlitzBoys", "Luigi");
            registrationService.addTeam(team2);

            TeamInputDTO team3 = new TeamInputDTO("ToadstoolTerrors", "Peach");
            registrationService.addTeam(team3);

            TeamInputDTO team4 = new TeamInputDTO("KoopaKings", "Bowser");
            registrationService.addTeam(team4);

            TeamInputDTO team5 = new TeamInputDTO("MushroomMasters", "Toad");
            registrationService.addTeam(team5);

            TeamInputDTO team6 = new TeamInputDTO("BulletBillBrigade", "Yoshi");
            registrationService.addTeam(team6);

            TeamInputDTO team7 = new TeamInputDTO("ChompChampions", "Donkey-Kong");
            registrationService.addTeam(team7);

            TeamInputDTO team8 = new TeamInputDTO("RainbowRiders", "Wario");
            registrationService.addTeam(team8);

            TeamInputDTO team9 = new TeamInputDTO("ShellShockers", "Waluigi");
            registrationService.addTeam(team9);

            TeamInputDTO team10 = new TeamInputDTO("BananaBandits", "Daisy");
            registrationService.addTeam(team10);

            TeamInputDTO team11 = new TeamInputDTO("PiranhaPals", "Rosalina");
            registrationService.addTeam(team11);

            TeamInputDTO team12 = new TeamInputDTO("ThwompThumpers", "Metall-Mario");
            registrationService.addTeam(team12);

            TeamInputDTO team13 = new TeamInputDTO("ShyGuySquad", "Shy-Guy");
            registrationService.addTeam(team13);

            TeamInputDTO team14 = new TeamInputDTO("DryBoneDynasty", "Knochentrocken");
            registrationService.addTeam(team14);

            TeamInputDTO team15 = new TeamInputDTO("LakituLegends", "Lakitu");
            registrationService.addTeam(team15);

            TeamInputDTO team16 = new TeamInputDTO("BooBusters", "König-Buu-Huu");
            registrationService.addTeam(team16);

            TeamInputDTO team17 = new TeamInputDTO("KoopaTroop", "Koopa");
            registrationService.addTeam(team17);

            TeamInputDTO team18 = new TeamInputDTO("InklingInvaders", "Inkling-Mädchen");
            registrationService.addTeam(team18);

            TeamInputDTO team19 = new TeamInputDTO("VillagerVictory", "Bewohner");
            registrationService.addTeam(team19);

            TeamInputDTO team20 = new TeamInputDTO("BabyBruisers", "Baby-Daisy");
            registrationService.addTeam(team20);

            TeamInputDTO team21 = new TeamInputDTO("Isabelle'sIsle", "Melinda");
            registrationService.addTeam(team21);
        } catch (IllegalArgumentException | EntityNotFoundException | RoundsAlreadyExistsException e) {
            System.err.print(e.getMessage());
        }

    }

    private void addRounds() {
        RoundInputDTO round1 = new RoundInputDTO(false);
        matchPlanService.addRound(round1);

        RoundInputDTO round2 = new RoundInputDTO(false);
        matchPlanService.addRound(round2);

        RoundInputDTO round3 = new RoundInputDTO(false);
        matchPlanService.addRound(round3);

        List<Round> rounds = roundRepository.findAll();
        for (int i = 0; i < rounds.size(); i++) {
            rounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(20L * i));
            rounds.get(i).setEndTime(LocalDateTime.now().plusMinutes(20L * i).plusMinutes(20));
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
                matchPlanService.addGame(game);

                Collections.shuffle(teams);

                List<Team> selectedTeams = teams.subList(0, 4);

                for (Team team : selectedTeams) {
                     Points point = new Points();
                     point.setGroupPoints(0);
                     point.setFinalPoints(0);
                     point.setTeam(team);
                     point.setGame(game);
                     matchPlanService.addPoints(point);
                }


            }
        }
    }
}
