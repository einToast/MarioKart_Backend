package de.fsr.mariokart_backend.match_plan.service.dto;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
        game.setRound(roundRepository   .findById(gameInputDTO.getRoundId())
                                        .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID.")));
        return game;
    }


}
