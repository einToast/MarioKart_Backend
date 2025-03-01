package de.fsr.mariokart_backend.match_plan.service.dto;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.match_plan.model.Break;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanInputDTOService {

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
