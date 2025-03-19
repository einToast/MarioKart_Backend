package de.fsr.mariokart_backend.schedule.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SCHEDULE)
public class AdminScheduleCreateController {

    private final AdminScheduleCreateService adminScheduleCreateService;

    // TODO: Update Error Handling
    @PostMapping("/create/match_plan")
    public List<RoundReturnDTO> createMatchPlan()
            throws RoundsAlreadyExistsException, NotEnoughTeamsException, EntityNotFoundException, RuntimeException {
        return adminScheduleCreateService.createMatchPlan();
    }

    @PostMapping("/create/final_plan")
    public List<RoundReturnDTO> createFinalPlan()
            throws RoundsAlreadyExistsException, NotEnoughTeamsException {
        return adminScheduleCreateService.createFinalPlan();
    }

    @PostMapping("/break")
    public BreakReturnDTO addBreak(@RequestBody BreakInputDTO breakCreation) throws EntityNotFoundException {
        return adminScheduleCreateService.addBreak(breakCreation);
    }
}