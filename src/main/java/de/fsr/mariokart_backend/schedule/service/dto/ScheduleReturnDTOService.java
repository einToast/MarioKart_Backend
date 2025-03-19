package de.fsr.mariokart_backend.schedule.service.dto;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.registration.service.dto.RegistrationReturnDTOService;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundFromBreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ScheduleReturnDTOService {

    private final RegistrationReturnDTOService registrationReturnDTOService;

    public GameReturnDTO gameToGameDTO(Game game) {
        if (game == null)
            return null;
        return new GameReturnDTO(game.getId(), game.getSwitchGame(),
                game.getPoints() != null ? game.getTeams().stream()
                        .map(registrationReturnDTOService::teamToTeamReturnDTO)
                        .collect(Collectors.toSet())
                        : null,
                game.getPoints() != null ? game.getPoints().stream()
                        .map(this::pointsToPointsDTO)
                        .collect(Collectors.toSet())
                        : null);
    }

    public RoundReturnDTO roundToRoundDTO(Round round) {
        if (round == null)
            return null;
        return new RoundReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(), round.getEndTime(),
                round.isFinalGame(), round.isPlayed(),
                round.getGames() != null ? round.getGames().stream()
                        .map(this::gameToGameDTO)
                        .collect(Collectors.toSet())
                        : null,
                round.getBreakTime()); // TODO: How is this not a circular reference?
    }

    public PointsReturnDTO pointsToPointsDTO(Points points) {
        if (points == null)
            return null;
        return new PointsReturnDTO(points.getId(), Math.max(points.getGroupPoints(), points.getFinalPoints()),
                registrationReturnDTOService.teamToTeamReturnDTO(points.getTeam()));
    }

    public BreakReturnDTO breakToBreakDTO(Break aBreak) {
        if (aBreak == null)
            return null;
        return new BreakReturnDTO(aBreak.getId(), aBreak.getStartTime(), aBreak.getEndTime(), aBreak.isBreakEnded(),
                roundToRoundFromBreakReturnDTO(aBreak.getRound()));
    }

    public RoundFromBreakReturnDTO roundToRoundFromBreakReturnDTO(Round round) {
        if (round == null)
            return null;
        return new RoundFromBreakReturnDTO(round.getId(), round.getRoundNumber(), round.getStartTime(),
                round.getEndTime(), round.isFinalGame(),
                round.isPlayed(),
                round.getGames() != null
                        ? round.getGames().stream().map(this::gameToGameDTO).collect(Collectors.toSet())
                        : null);
    }
}
