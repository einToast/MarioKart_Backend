package de.fsr.mariokart_backend;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.AddCharacterService;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsCreateService;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyCreateService;
import de.fsr.mariokart_backend.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class MarioKartStartupRunnerTest {

    @Mock
    private PublicRegistrationCreateService publicRegistrationCreateService;

    @Mock
    private AdminScheduleCreateService adminScheduleCreateService;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AddCharacterService addCharacterService;

    @Mock
    private UserService userService;

    @Mock
    private AdminSettingsCreateService adminSettingsCreateService;

    @Mock
    private AdminSettingsUpdateService adminSettingsUpdateService;

    @Mock
    private AdminSurveyCreateService adminSurveyCreateService;

    @InjectMocks
    private MarioKartStartupRunner startupRunner;

    @Test
    void runInvokesBootstrapAndReturnsWhenUserEnvMissing() throws Exception {
        startupRunner.run();

        verify(addCharacterService).addCharacters("media");
        verify(adminSettingsCreateService).createSettings();
        verify(userService, never()).getUsers();
    }

    @Test
    void runSwallowsBootstrapExceptionAndContinues() throws Exception {
        doThrow(new IOException("boom")).when(addCharacterService).addCharacters("media");

        startupRunner.run();

        verify(adminSettingsCreateService, never()).createSettings();
        verify(userService, never()).getUsers();
    }
}
