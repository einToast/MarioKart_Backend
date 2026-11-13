package de.fsr.mariokart_backend;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.seed.SeedProperties;
import de.fsr.mariokart_backend.seed.service.EventSeedService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsCreateService;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class MarioKartStartupRunner implements CommandLineRunner {
    private final AddCharacterService addCharacterService;
    private final UserService userService;
    private final AdminSettingsCreateService adminSettingsCreateService;
    private final EventSeedService eventSeedService;
    private final SeedProperties seedProperties;

    @Override
    public void run(String @NonNull... args) throws Exception {
        try {
            addCharacterService.addCharacters();
            adminSettingsCreateService.createSettings();
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        }

        addUser();
        eventSeedService.seed(seedProperties.getEvent());
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
}
