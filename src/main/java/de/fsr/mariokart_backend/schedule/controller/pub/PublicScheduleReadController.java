package de.fsr.mariokart_backend.schedule.controller.pub;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.SCHEDULE)
public class PublicScheduleReadController {

    private final PublicScheduleReadService publicScheduleReadService;

    @GetMapping("/rounds/current")
    public List<RoundReturnDTO> getCurrentRounds() {
        return publicScheduleReadService.getCurrentRounds();
    }

    @GetMapping("/rounds/unplayed")
    public Integer getNumberOfRoundsUnplayed() {
        return publicScheduleReadService.getNumberOfRoundsUnplayed();
    }

    @GetMapping("/create/schedule")
    public Boolean isScheduleCreated() {
        return publicScheduleReadService.isScheduleCreated();
    }

    @GetMapping("/create/final_schedule")
    public Boolean isFinalScheduleCreated() {
        return publicScheduleReadService.isFinalScheduleCreated();
    }
}