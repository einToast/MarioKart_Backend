package de.fsr.mariokart_backend.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.RegistrationCreateService;
import lombok.AllArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class RegistrationCreateController {

    private final RegistrationCreateService registrationCreateService;

    @PostMapping
    public ResponseEntity<TeamReturnDTO> addTeam(@RequestBody TeamInputDTO teamCreation) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(registrationCreateService.addTeam(teamCreation));
        } catch (RoundsAlreadyExistsException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 