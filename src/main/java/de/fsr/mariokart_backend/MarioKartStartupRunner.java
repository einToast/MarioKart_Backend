package de.fsr.mariokart_backend;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsCreateService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class MarioKartStartupRunner implements CommandLineRunner {
    private final PublicRegistrationCreateService publicRegistrationCreateService;
    private final TeamRepository teamRepository;
    private final AddCharacterService addCharacterService;
    private final UserService userService;
    private final AdminSettingsCreateService adminSettingsCreateService;
    private final AdminSettingsUpdateService adminSettingsUpdateService;

    @Override
    public void run(String @NonNull... args) throws Exception {
        try {
            addCharacterService.addCharacters();
            adminSettingsCreateService.createSettings();
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        }

        addUser();
        addTeams();

    }

    private void addUser() {
        String username = System.getenv("USER_NAME");
        String password = System.getenv("USER_PASSWORD");

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            log.warn("Skipping admin bootstrap user: USER_NAME or USER_PASSWORD is not set.");
            return;
        }

        if (userService.getUsers().isEmpty()) {
            try {
                if (userService.getUser(username) != null) {
                    log.info("User already exists!");
                    return;
                }
            } catch (EntityNotFoundException e) {
                log.info(e.getMessage());
            }
            User user = new User(username, true);
            user.setPassword(password);
            userService.createAndRegisterIfNotExist(user);
        }

    }

    private void addTeams() {
        if (teamRepository.count() > 0) {
            log.info("Teams already exist!");
            return;
        }
        try {
            adminSettingsUpdateService.updateSettings(new TournamentDTO(true, true, 6,
                    null));
            TeamInputDTO team1 = new TeamInputDTO("GreatTeamName", "Mario");
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
            log.warn(e.getMessage());
        }

    }
}
