package de.fsr.mariokart_backend;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.seed.SeedProperties;
import de.fsr.mariokart_backend.seed.service.EventSeedService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsCreateService;
import de.fsr.mariokart_backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MarioKartStartupRunnerTest {

    @Mock
    private AddCharacterService addCharacterService;

    @Mock
    private UserService userService;

    @Mock
    private AdminSettingsCreateService adminSettingsCreateService;

    @Mock
    private EventSeedService eventSeedService;

    @Mock
    private SeedProperties seedProperties;

    @InjectMocks
    private MarioKartStartupRunner startupRunner;

    @Test
    void runInvokesBootstrapAndReturnsWhenUserEnvMissing() throws Exception {
        startupRunner.run();

        verify(addCharacterService).addCharacters();
        verify(adminSettingsCreateService).createSettings();
        verify(userService, never()).getUsers();
    }

    @Test
    void runSwallowsBootstrapExceptionAndContinues() throws Exception {
        doThrow(new IllegalStateException("Characters already exist.")).when(addCharacterService).addCharacters();

        startupRunner.run();

        verify(adminSettingsCreateService, never()).createSettings();
        verify(userService, never()).getUsers();
    }

    @Test
    void runSeedsTheConfiguredEvent() throws Exception {
        when(seedProperties.getEvent()).thenReturn("demo");

        startupRunner.run();

        verify(eventSeedService).seed("demo");
    }
}
