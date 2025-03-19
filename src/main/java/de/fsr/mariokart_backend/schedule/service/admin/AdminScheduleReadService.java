package de.fsr.mariokart_backend.schedule.service.admin;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminScheduleReadService {

    private final RoundRepository roundRepository;
    private final BreakRepository breakRepository;
    private final ScheduleReturnDTOService scheduleReturnDTOService;

    public List<RoundReturnDTO> getRounds() {
        return roundRepository.findAll().stream()
                .sorted(Comparator.comparing(Round::getRoundNumber))
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .toList();
    }

    public RoundReturnDTO getRoundById(Long roundId) throws EntityNotFoundException {
        return roundRepository.findById(roundId)
                .map(scheduleReturnDTOService::roundToRoundDTO)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
    }

    public BreakReturnDTO getBreak() {
        return scheduleReturnDTOService.breakToBreakDTO(breakRepository.findAll().get(0));
    }

}