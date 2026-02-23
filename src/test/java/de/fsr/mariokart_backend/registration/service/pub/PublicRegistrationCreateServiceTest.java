package de.fsr.mariokart_backend.registration.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationInputDTOService;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import de.fsr.mariokart_backend.testsupport.TestDataFactory;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicRegistrationCreateServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PublicSettingsReadService settingsReadService;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @Mock
    private RegistrationInputDTOService registrationInputDTOService;

    @Mock
    private RegistrationReturnDTOService registrationReturnDTOService;

    @InjectMocks
    private PublicRegistrationCreateService service;

    @Test
    void registerTeamPersistsAndReturnsDto() throws Exception {
        TeamInputDTO input = TestDataFactory.teamInput("Speedsters", "Mario");
        Team team = TestDataFactory.team("Speedsters", TestDataFactory.character("Mario"));
        TeamReturnDTO expected = TestDataFactory.teamReturn(1L, "Speedsters", "Mario");

        when(settingsReadService.getSettings()).thenReturn(TestDataFactory.openTournamentSettings());
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(registrationInputDTOService.teamInputDTOToTeam(input)).thenReturn(team);
        when(teamRepository.existsByTeamName("Speedsters")).thenReturn(false);
        when(teamRepository.save(team)).thenReturn(team);
        when(registrationReturnDTOService.teamToTeamReturnDTO(team)).thenReturn(expected);

        TeamReturnDTO result = service.registerTeam(input);

        assertThat(result).isEqualTo(expected);
        assertThat(team.isActive()).isTrue();
        assertThat(team.isFinalReady()).isTrue();
        assertThat(team.getCharacter().getTeam()).isEqualTo(team);
    }

    @Test
    void registerTeamThrowsWhenScheduleExists() {
        TeamInputDTO input = TestDataFactory.teamInput("Speedsters", "Mario");
        TournamentDTO open = TestDataFactory.openTournamentSettings();

        when(settingsReadService.getSettings()).thenReturn(open);
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.registerTeam(input))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Schedule already exists");
    }

    @Test
    void registerTeamThrowsWhenTeamNameExists() throws EntityNotFoundException {
        TeamInputDTO input = TestDataFactory.teamInput("Speedsters", "Mario");
        Team team = TestDataFactory.team("Speedsters", TestDataFactory.character("Mario"));

        when(settingsReadService.getSettings()).thenReturn(TestDataFactory.openTournamentSettings());
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(registrationInputDTOService.teamInputDTOToTeam(input)).thenReturn(team);
        when(teamRepository.existsByTeamName("Speedsters")).thenReturn(true);

        assertThatThrownBy(() -> service.registerTeam(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Team name already exists");
    }
}
