package de.fsr.mariokart_backend.registration.controller.pub;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.pub.PublicRegistrationCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.REGISTRATION)
public class PublicRegistrationCreateController {

    private final PublicRegistrationCreateService publicRegistrationCreateService;

    @PostMapping
    public ResponseEntity<TeamReturnDTO> registerTeam(@RequestBody TeamInputDTO teamCreation) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(publicRegistrationCreateService.registerTeam(teamCreation));
        } catch (RoundsAlreadyExistsException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}