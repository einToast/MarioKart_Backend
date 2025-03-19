package de.fsr.mariokart_backend.schedule.controller.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleDeleteService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SCHEDULE)
public class AdminScheduleDeleteController {

    private final AdminScheduleDeleteService adminScheduleDeleteService;

    @DeleteMapping("/create/match_plan")
    public void deleteMatchPlan() {
        adminScheduleDeleteService.deleteMatchPlan();
    }

    @DeleteMapping("/create/final_plan")
    public void deleteFinalPlan() {
        adminScheduleDeleteService.deleteFinalPlan();
    }
}