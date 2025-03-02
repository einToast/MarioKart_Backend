package de.fsr.mariokart_backend.match_plan.service.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.match_plan.model.Break;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameFromPointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameFromRoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsFromGameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundFromBreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundFromGameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationFromMatchPlanReturnDTOService;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanReturnDTOService {

    // private final RegistrationReturnDTOService registrationReturnDTOService;
    private final SettingsService settingsService;
    private final RegistrationFromMatchPlanReturnDTOService registrationFromMatchPlanReturnDTOService;

    public GameReturnDTO gameToGameDTO(Game game) {
        if (game == null)
            return null;
        return new GameReturnDTO(game.getId(), game.getSwitchGame(), roundToRoundFromGameReturnDTO(game.getRound()),
                game.getPoints() != null ? game.getTeams().stream()
                        .map(registrationFromMatchPlanReturnDTOService::teamToTeamFromGameReturnDTO)
                        .collect(Collectors.toSet()) : null,
                game.getPoints() != null
                        ? game.getPoints().stream().map(this::pointsToPointsFromGameReturnDTO)
                                .collect(Collectors.toSet())
                        : null);
    }

    public RoundReturnDTO roundToRoundDTO(Round round) {
        if (round == null)
            return null;
        return new RoundReturnDTO(round.getId(), round.getStartTime(), round.getEndTime(), round.isFinalGame(),
                round.isPlayed(),
                round.getGames() != null
                        ? round.getGames().stream().map(this::gameToGameFromRoundReturnDTO).collect(Collectors.toSet())
                        : null,
                round.getBreakTime());
    }

    public PointsReturnDTO pointsToPointsDTO(Points points) {
        if (points == null)
            return null;
        return new PointsReturnDTO(points.getId(), Math.max(points.getGroupPoints(), points.getFinalPoints()),
                registrationFromMatchPlanReturnDTOService.teamToTeamFromPointsReturnDTO(points.getTeam()),
                gameToGameFromPointsReturnDTO(points.getGame()));
    }

    public BreakReturnDTO breakToBreakDTO(Break aBreak) {
        if (aBreak == null)
            return null;
        return new BreakReturnDTO(aBreak.getId(), aBreak.getStartTime(), aBreak.getEndTime(), aBreak.isBreakEnded(),
                roundToRoundFromBreakReturnDTO(aBreak.getRound()));
    }

    public GameFromRoundReturnDTO gameToGameFromRoundReturnDTO(Game game) {
        if (game == null)
            return null;
        return new GameFromRoundReturnDTO(game.getId(), game.getSwitchGame(),
                game.getTeams().stream().map(registrationFromMatchPlanReturnDTOService::teamToTeamFromGameReturnDTO)
                        .collect(Collectors.toSet()),
                game.getPoints().stream().map(this::pointsToPointsFromGameReturnDTO).collect(Collectors.toSet()));
    }

    public GameFromPointsReturnDTO gameToGameFromPointsReturnDTO(Game game) {
        if (game == null)
            return null;
        return new GameFromPointsReturnDTO(game.getId(), game.getSwitchGame(),
                roundToRoundFromGameReturnDTO(game.getRound()));
    }

    public RoundFromGameReturnDTO roundToRoundFromGameReturnDTO(Round round) {
        if (round == null)
            return null;
        return new RoundFromGameReturnDTO(round.getId(), round.getStartTime(), round.getEndTime(), round.isFinalGame(),
                round.isPlayed(), round.getBreakTime());
    }

    public RoundFromBreakReturnDTO roundToRoundFromBreakReturnDTO(Round round) {
        if (round == null)
            return null;
        return new RoundFromBreakReturnDTO(round.getId(), round.getStartTime(), round.getEndTime(), round.isFinalGame(),
                round.isPlayed(),
                round.getGames() != null
                        ? round.getGames().stream().map(this::gameToGameFromRoundReturnDTO).collect(Collectors.toSet())
                        : null);
    }

    public PointsFromGameReturnDTO pointsToPointsFromGameReturnDTO(Points points) {
        if (points == null)
            return null;
        return new PointsFromGameReturnDTO(points.getId(), Math.max(points.getGroupPoints(), points.getFinalPoints()),
                registrationFromMatchPlanReturnDTOService.teamToTeamFromPointsReturnDTO(points.getTeam()));
    }
}
