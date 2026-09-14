package de.fsr.mariokart_backend.schedule.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminScheduleReadServiceTest {

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private BreakRepository breakRepository;

    @Mock
    private ScheduleReturnDTOService scheduleReturnDTOService;

    @InjectMocks
    private AdminScheduleReadService service;

    @Test
    void getRoundsSortsByRoundNumberAndMaps() {
        Round roundTwo = new Round();
        roundTwo.setId(2L);
        roundTwo.setRoundNumber(2);
        Round roundOne = new Round();
        roundOne.setId(1L);
        roundOne.setRoundNumber(1);

        RoundReturnDTO dtoOne = new RoundReturnDTO();
        dtoOne.setId(1L);
        RoundReturnDTO dtoTwo = new RoundReturnDTO();
        dtoTwo.setId(2L);

        when(roundRepository.findAll()).thenReturn(List.of(roundTwo, roundOne));
        when(scheduleReturnDTOService.roundToRoundDTO(roundOne)).thenReturn(dtoOne);
        when(scheduleReturnDTOService.roundToRoundDTO(roundTwo)).thenReturn(dtoTwo);

        List<RoundReturnDTO> result = service.getRounds();

        assertThat(result).containsExactly(dtoOne, dtoTwo);
    }

    @Test
    void getRoundByIdReturnsMappedRound() throws Exception {
        Round round = new Round();
        round.setId(3L);
        RoundReturnDTO dto = new RoundReturnDTO();
        dto.setId(3L);

        when(roundRepository.findById(3L)).thenReturn(Optional.of(round));
        when(scheduleReturnDTOService.roundToRoundDTO(round)).thenReturn(dto);

        RoundReturnDTO result = service.getRoundById(3L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getRoundByIdThrowsWhenMissing() {
        when(roundRepository.findById(4L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRoundById(4L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("round with this ID");
    }

    @Test
    void getBreakReturnsMappedFirstBreak() {
        Break aBreak = new Break();
        aBreak.setId(1L);
        BreakReturnDTO dto = new BreakReturnDTO();
        dto.setId(1L);

        when(breakRepository.findAll()).thenReturn(List.of(aBreak));
        when(scheduleReturnDTOService.breakToBreakDTO(aBreak)).thenReturn(dto);

        BreakReturnDTO result = service.getBreak();

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void isBreakFinishedReturnsFlagFromFirstBreak() {
        Break aBreak = new Break();
        aBreak.setBreakEnded(true);

        when(breakRepository.findAll()).thenReturn(List.of(aBreak));

        assertThat(service.isBreakFinished()).isTrue();
    }
}
