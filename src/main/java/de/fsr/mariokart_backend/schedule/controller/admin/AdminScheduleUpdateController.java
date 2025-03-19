package de.fsr.mariokart_backend.schedule.controller.admin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SCHEDULE)
public class AdminScheduleUpdateController {

    private final AdminScheduleUpdateService adminScheduleUpdateService;

    @PutMapping("/rounds/{roundId}")
    public RoundReturnDTO updateRoundPlayed(@PathVariable Long roundId, @RequestBody RoundInputDTO roundCreation)
            throws EntityNotFoundException, RoundsAlreadyExistsException {
        return adminScheduleUpdateService.updateRoundPlayed(roundId, roundCreation);
    }

    @PutMapping("/rounds/{roundId}/games/{gameId}/teams/{teamId}/points")
    public PointsReturnDTO updatePoints(
            @PathVariable Long roundId,
            @PathVariable Long gameId,
            @PathVariable Long teamId,
            @RequestBody PointsInputDTO pointsCreation) throws EntityNotFoundException {
        return adminScheduleUpdateService.updatePoints(roundId, gameId, teamId, pointsCreation);
    }

    @PutMapping("/break")
    public BreakReturnDTO updateBreak(@RequestBody BreakInputDTO breakCreation) throws EntityNotFoundException {
        return adminScheduleUpdateService.updateBreak(breakCreation);
    }
}