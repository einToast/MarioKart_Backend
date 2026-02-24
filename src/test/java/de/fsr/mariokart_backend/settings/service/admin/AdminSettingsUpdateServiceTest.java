package de.fsr.mariokart_backend.settings.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSettingsUpdateServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private PublicScheduleReadService publicScheduleReadService;

    @InjectMocks
    private AdminSettingsUpdateService service;

    @Test
    void updateSettingsUpdatesProvidedFields() throws RoundsAlreadyExistsException {
        Tournament existing = new Tournament(1L, false, false, 4);
        Tournament saved = new Tournament(1L, true, true, 8);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(false);
        when(tournamentRepository.save(existing)).thenReturn(saved);

        TournamentDTO result = service.updateSettings(new TournamentDTO(true, true, 8));

        assertThat(result.getTournamentOpen()).isTrue();
        assertThat(result.getRegistrationOpen()).isTrue();
        assertThat(result.getMaxGamesCount()).isEqualTo(8);
    }

    @Test
    void updateSettingsThrowsConflictWhenReopeningRegistrationAfterScheduleCreation() {
        Tournament existing = new Tournament(1L, false, false, 4);

        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(existing)));
        when(publicScheduleReadService.isScheduleCreated()).thenReturn(true);

        assertThatThrownBy(() -> service.updateSettings(new TournamentDTO(null, true, null)))
                .isInstanceOf(RoundsAlreadyExistsException.class)
                .hasMessageContaining("Matches already exist");
    }

    @Test
    void updateSettingsThrowsWhenSettingsMissing() {
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> service.updateSettings(new TournamentDTO(true, true, 3)))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
