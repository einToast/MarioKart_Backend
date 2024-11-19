package de.fsr.mariokart_backend.match_plan.service.dto;

import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.*;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationFromMatchPlanReturnDTOService;
import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MatchPlanReturnDTOService {

//    private final RegistrationReturnDTOService registrationReturnDTOService;
    private final SettingsService settingsService;
    private final RegistrationFromMatchPlanReturnDTOService registrationFromMatchPlanReturnDTOService;

    public GameReturnDTO gameToGameDTO(Game game){
        if (game == null)
            return null;
        return new GameReturnDTO(game.getId(), game.getSwitchGame(), roundToRoundFromGameReturnDTO(game.getRound()), game.getPoints() != null ? game.getTeams().stream().map(registrationFromMatchPlanReturnDTOService::teamToTeamFromGameReturnDTO).collect(Collectors.toSet()) : null, game.getPoints() != null ? game.getPoints().stream().map(this::pointsToPointsFromGameReturnDTO).collect(Collectors.toSet()) : null);
    }

    public RoundReturnDTO roundToRoundDTO(Round round){
        if (round == null)
            return null;
        return new RoundReturnDTO(round.getId(), round.getStartTime(), round.getEndTime(), round.isFinalGame(), round.isPlayed(), round.getGames() != null ? round.getGames().stream().map(this::gameToGameFromRoundReturnDTO).collect(Collectors.toSet()) : null, round.getBreakTime());
    }

    public PointsReturnDTO pointsToPointsDTO(Points points){
        if (points == null)
            return null;
        return new PointsReturnDTO(points.getId(), Math.max(points.getGroupPoints(), points.getFinalPoints()), registrationFromMatchPlanReturnDTOService.teamToTeamFromPointsReturnDTO(points.getTeam()), gameToGameFromPointsReturnDTO(points.getGame()));
    }

    public GameFromRoundReturnDTO gameToGameFromRoundReturnDTO (Game game){
        if (game == null)
            return null;
        return new GameFromRoundReturnDTO(game.getId(), game.getSwitchGame(), game.getTeams().stream().map(registrationFromMatchPlanReturnDTOService::teamToTeamFromGameReturnDTO).collect(Collectors.toSet()), game.getPoints().stream().map(this::pointsToPointsFromGameReturnDTO).collect(Collectors.toSet()));
    }

    public GameFromPointsReturnDTO gameToGameFromPointsReturnDTO (Game game){
        if (game == null)
            return null;
        return new GameFromPointsReturnDTO(game.getId(), game.getSwitchGame(), roundToRoundFromGameReturnDTO(game.getRound()));
    }

    public RoundFromGameReturnDTO roundToRoundFromGameReturnDTO (Round round){
        if (round == null)
            return null;
        return new RoundFromGameReturnDTO(round.getId(), round.getStartTime(), round.getEndTime(), round.isFinalGame(), round.isPlayed(), round.getBreakTime());
    }

    public PointsFromGameReturnDTO pointsToPointsFromGameReturnDTO (Points points){
        if (points == null)
            return null;
        return new PointsFromGameReturnDTO(points.getId(), Math.max(points.getGroupPoints(), points.getFinalPoints()), registrationFromMatchPlanReturnDTOService.teamToTeamFromPointsReturnDTO(points.getTeam()));
    }
}
