package de.fsr.mariokart_backend.schedule.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.service.admin.AdminScheduleReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SCHEDULE)
public class AdminScheduleReadController {

    private final AdminScheduleReadService adminScheduleReadService;

    @GetMapping("/rounds")
    public List<RoundReturnDTO> getRounds() {
        return adminScheduleReadService.getRounds();
    }

    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<RoundReturnDTO> getRoundById(@PathVariable Long roundId) {
        try {
            return ResponseEntity.ok(adminScheduleReadService.getRoundById(roundId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/break")
    public ResponseEntity<BreakReturnDTO> getBreak() {
        return ResponseEntity.ok(adminScheduleReadService.getBreak());
    }

}