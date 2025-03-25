package de.fsr.mariokart_backend.registration.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.REGISTRATION)
public class AdminRegistrationReadController {

    private final AdminRegistrationReadService adminRegistrationReadService;

    @GetMapping("/sortedByFinalPoints")
    public List<TeamReturnDTO> getTeamsSortedByFinalPoints() {
        return adminRegistrationReadService.getTeamsSortedByFinalPoints();
    }

    @GetMapping("/finalTeams")
    public List<TeamReturnDTO> getFinalTeams() {
        return adminRegistrationReadService.getFinalTeamsReturnDTO();
    }
}