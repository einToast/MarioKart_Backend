package de.fsr.mariokart_backend.settings.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
class AdminSettingsCreateServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @InjectMocks
    private AdminSettingsCreateService service;

    @Test
    void createSettingsCreatesDefaultTournamentWhenMissing() {
        Tournament saved = new Tournament(1L, true, true, 6);

        when(tournamentRepository.findAll()).thenReturn(List.of());
        when(tournamentRepository.save(org.mockito.ArgumentMatchers.any(Tournament.class))).thenReturn(saved);

        TournamentDTO dto = service.createSettings();

        assertThat(dto.getTournamentOpen()).isTrue();
        assertThat(dto.getRegistrationOpen()).isTrue();
        assertThat(dto.getMaxGamesCount()).isEqualTo(6);
    }

    @Test
    void createSettingsThrowsWhenAlreadyPresent() {
        when(tournamentRepository.findAll()).thenReturn(List.of(new Tournament(1L, true, true, 6)));

        assertThatThrownBy(() -> service.createSettings())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exist");
    }
}
