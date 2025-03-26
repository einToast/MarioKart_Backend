package de.fsr.mariokart_backend.schedule.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleInputDTOService {

    private final RoundRepository roundRepository;

    public Round roundInputDTOToRound(RoundInputDTO roundInputDTO) {
        Round round = new Round();
        round.setPlayed(roundInputDTO.isPlayed());
        return round;
    }

    public Game gameInputDTOToGame(GameInputDTO gameInputDTO) throws EntityNotFoundException {
        Game game = new Game();
        game.setSwitchGame(gameInputDTO.getSwitchGame());
        game.setRound(roundRepository.findById(gameInputDTO.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID.")));
        return game;
    }

    public Break breakInputDTOToBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        Break aBreak = new Break();
        Round round = roundRepository.findById(breakCreation.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        aBreak.setRound(round);
        aBreak.setStartTime(round.getStartTime());
        aBreak.setEndTime(round.getStartTime().plusMinutes(breakCreation.getBreakDuration()));
        aBreak.setBreakEnded(breakCreation.getBreakEnded());
        return aBreak;
    }
}
