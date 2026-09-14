package de.fsr.mariokart_backend.schedule.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ScheduleInputDTOServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private ScheduleInputDTOService service;

    @Test
    void roundInputDTOToRoundMapsPlayedFlag() {
        Round result = service.roundInputDTOToRound(new RoundInputDTO(true));

        assertThat(result.isPlayed()).isTrue();
    }

    @Test
    void gameInputDTOToGameMapsFieldsAndRound() throws Exception {
        Round round = new Round();
        round.setId(5L);

        when(roundRepository.findById(5L)).thenReturn(Optional.of(round));

        Game result = service.gameInputDTOToGame(new GameInputDTO(5L, "Blau"));

        assertThat(result.getSwitchGame()).isEqualTo("Blau");
        assertThat(result.getRound()).isEqualTo(round);
    }

    @Test
    void gameInputDTOToGameThrowsWhenRoundMissing() {
        when(roundRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.gameInputDTOToGame(new GameInputDTO(5L, "Blau")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("round with this ID");
    }

    @Test
    void breakInputDTOToBreakBuildsBreakFromRoundAndDuration() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0);
        Round round = new Round();
        round.setId(3L);
        round.setStartTime(start);

        when(roundRepository.findById(3L)).thenReturn(Optional.of(round));

        Break result = service.breakInputDTOToBreak(new BreakInputDTO(3L, 15, true));

        assertThat(result.getRound()).isEqualTo(round);
        assertThat(result.getStartTime()).isEqualTo(start);
        assertThat(result.getEndTime()).isEqualTo(start.plusMinutes(15));
        assertThat(result.isBreakEnded()).isTrue();
    }

    @Test
    void breakInputDTOToBreakThrowsWhenRoundMissing() {
        when(roundRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.breakInputDTOToBreak(new BreakInputDTO(7L, 10, false)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("round with this ID");
    }
}
