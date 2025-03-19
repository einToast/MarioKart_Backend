package de.fsr.mariokart_backend.registration.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationDeleteService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.REGISTRATION)
public class AdminRegistrationDeleteController {

    private final AdminRegistrationDeleteService adminRegistrationDeleteService;

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        try {
            adminRegistrationDeleteService.deleteTeam(id);
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping
    public void deleteAllTeams() {
        try {
            adminRegistrationDeleteService.deleteAllTeams();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}