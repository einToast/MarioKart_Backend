package de.fsr.mariokart_backend.settings.service.pub;

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

import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicSettingsReadServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private PublicSettingsReadService service;

    @Test
    void getSettingsReturnsFirstTournament() {
        Tournament tournament = new Tournament(1L, true, true, 6);
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>(List.of(tournament)));

        TournamentDTO dto = service.getSettings();

        assertThat(dto.getTournamentOpen()).isTrue();
        assertThat(dto.getRegistrationOpen()).isTrue();
        assertThat(dto.getMaxGamesCount()).isEqualTo(6);
    }

    @Test
    void getSettingsThrowsWhenSettingsMissing() {
        when(tournamentRepository.findAll()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> service.getSettings())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Settings do not exist");
    }
}
