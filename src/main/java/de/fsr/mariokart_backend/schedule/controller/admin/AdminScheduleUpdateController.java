package de.fsr.mariokart_backend.schedule.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SCHEDULE)
public class AdminScheduleUpdateController {

    private final AdminScheduleUpdateService adminScheduleUpdateService;

    @PutMapping("/rounds/{roundId}")
    public RoundReturnDTO updateRoundPlayed(@PathVariable Long roundId, @RequestBody RoundInputDTO roundCreation) {
        try {
            return adminScheduleUpdateService.updateRoundPlayed(roundId, roundCreation);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/rounds/{roundId}/games/{gameId}/teams/{teamId}/points")
    public PointsReturnDTO updatePoints(
            @PathVariable Long roundId,
            @PathVariable Long gameId,
            @PathVariable Long teamId,
            @RequestBody PointsInputDTO pointsCreation) {
        try {
            return adminScheduleUpdateService.updatePoints(roundId, gameId, teamId, pointsCreation);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/break")
    public BreakReturnDTO updateBreak(@RequestBody BreakInputDTO breakCreation) {
        try {
            return adminScheduleUpdateService.updateBreak(breakCreation);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/rounds/{roundId}/full")
    public RoundReturnDTO updateRoundFull(@PathVariable Long roundId, @RequestBody RoundInputFullDTO roundCreation) {
        try {
            return adminScheduleUpdateService.updateRound(roundId, roundCreation);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    @PutMapping("/games/{gameId}")
    public GameReturnDTO updateGame(@PathVariable Long gameId, @RequestBody GameInputFullDTO gameInput) {
        try {
            return adminScheduleUpdateService.updateGame(gameId, gameInput);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}