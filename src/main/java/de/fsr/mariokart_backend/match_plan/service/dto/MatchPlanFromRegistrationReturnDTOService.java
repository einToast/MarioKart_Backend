package de.fsr.mariokart_backend.match_plan.service.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.GameFromTeamReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsFromGameFromTeamReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundFromGameFromTeamReturnDTO;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanFromRegistrationReturnDTOService {

    // private final MatchPlanReturnDTOService matchPlanReturnDTOService;

    public GameFromTeamReturnDTO gameToGameFromTeamReturnDTO(Game game) {
        if (game == null)
            return null;
        return new GameFromTeamReturnDTO(game.getId(), game.getSwitchGame(),
                roundToRoundFromGameFromRegistrationReturnDTO(game.getRound()), game.getPoints().stream()
                        .map(this::pointsToPointsFromGameFromTeamReturnDTO).collect(Collectors.toSet()));
    }

    public RoundFromGameFromTeamReturnDTO roundToRoundFromGameFromRegistrationReturnDTO(Round round) {
        if (round == null)
            return null;
        return new RoundFromGameFromTeamReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(),
                round.getEndTime(), round.isFinalGame(), round.isPlayed());
    }

    public PointsFromGameFromTeamReturnDTO pointsToPointsFromGameFromTeamReturnDTO(Points points) {
        if (points == null)
            return null;
        return new PointsFromGameFromTeamReturnDTO(points.getId(),
                Math.max(points.getGroupPoints(), points.getFinalPoints()));
    }

}
