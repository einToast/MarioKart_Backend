package de.fsr.mariokart_backend.registration.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.REGISTRATION)
public class AdminRegistrationUpdateController {

    private final AdminRegistrationUpdateService adminRegistrationUpdateService;

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<TeamReturnDTO> updateTeam(@PathVariable Long id, @RequestBody TeamInputDTO teamCreation) {
        try {
            return ResponseEntity.ok(adminRegistrationUpdateService.updateTeam(id, teamCreation));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/finalParticipation/reset")
    public List<TeamReturnDTO> resetEveryTeamFinalParticipation() {
        return adminRegistrationUpdateService.resetEveryTeamFinalParticipation();
    }
}
