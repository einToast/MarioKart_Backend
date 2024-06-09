package de.fsr.mariokart_backend;

import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundDTO;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanService;
import de.fsr.mariokart_backend.registration.model.dto.TeamDTO;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.service.RegistrationService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class MyStartupRunner implements CommandLineRunner {
    private final RegistrationService registrationService;
    private final MatchPlanService matchPlanService;

    @Override
    public void run(String... args) throws Exception {
        addTeams();
        addRounds();
        addGames();

    }

    private void addTeams() {
        TeamDTO team1 = new TeamDTO("TollerTeamName", "Mario");
        registrationService.addTeam(team1);

        TeamDTO team2 = new TeamDTO("BlitzBoys", "Luigi");
        registrationService.addTeam(team2);

        TeamDTO team3 = new TeamDTO("ToadstoolTerrors", "Peach");
        registrationService.addTeam(team3);

        TeamDTO team4 = new TeamDTO("KoopaKings", "Bowser");
        registrationService.addTeam(team4);

        TeamDTO team5 = new TeamDTO("MushroomMasters", "Toad");
        registrationService.addTeam(team5);

        TeamDTO team6 = new TeamDTO("BulletBillBrigade", "Yoshi");
        registrationService.addTeam(team6);

        TeamDTO team7 = new TeamDTO("ChompChampions", "Donkey-Kong");
        registrationService.addTeam(team7);

        TeamDTO team8 = new TeamDTO("RainbowRiders", "Wario");
        registrationService.addTeam(team8);

        TeamDTO team9 = new TeamDTO("ShellShockers", "Waluigi");
        registrationService.addTeam(team9);

        TeamDTO team10 = new TeamDTO("BananaBandits", "Daisy");
        registrationService.addTeam(team10);

        TeamDTO team11 = new TeamDTO("PiranhaPals", "Rosalina");
        registrationService.addTeam(team11);

        TeamDTO team12 = new TeamDTO("ThwompThumpers", "Metall-Mario");
        registrationService.addTeam(team12);

        TeamDTO team13 = new TeamDTO("ShyGuySquad", "Shy-Guy");
        registrationService.addTeam(team13);

        TeamDTO team14 = new TeamDTO("DryBoneDynasty", "Knochentrocken");
        registrationService.addTeam(team14);

        TeamDTO team15 = new TeamDTO("LakituLegends", "Lakitu");
        registrationService.addTeam(team15);

        TeamDTO team16 = new TeamDTO("BooBusters", "König-Buu-Huu");
        registrationService.addTeam(team16);

        TeamDTO team17 = new TeamDTO("KoopaTroop", "Koopa");
        registrationService.addTeam(team17);

        TeamDTO team18 = new TeamDTO("InklingInvaders", "Inkling-Mädchen");
        registrationService.addTeam(team18);

        TeamDTO team19 = new TeamDTO("VillagerVictory", "Bewohner");
        registrationService.addTeam(team19);

        TeamDTO team20 = new TeamDTO("BabyBruisers", "Baby Daisy");
        registrationService.addTeam(team20);

        TeamDTO team21 = new TeamDTO("Isabelle'sIsle", "Melinda");
        registrationService.addTeam(team21);

    }

    private void addRounds() {
        RoundDTO round1 = new RoundDTO(false);
        matchPlanService.addRound(round1);

        RoundDTO round2 = new RoundDTO(false);
        matchPlanService.addRound(round2);

        RoundDTO round3 = new RoundDTO(false);
        matchPlanService.addRound(round3);
    }

    private void addGames() {
        List<Round> rounds = matchPlanService.getRounds();
        List<Team> teams = registrationService.getTeams();
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
                    game.setSwitchGame("Gelb");
                }
                game.setRound(round);
                matchPlanService.addGame(game);

                Collections.shuffle(teams);

                List<Team> selectedTeams = teams.subList(0, 4);

                for (Team team : selectedTeams) {
                     Points point = new Points();
                     point.setNormal_points(1);
                     point.setFinal_points(0);
                     point.setTeam(team);
                     point.setGame(game);
                     matchPlanService.addPoints(point);
                }


            }
        }
    }
}
