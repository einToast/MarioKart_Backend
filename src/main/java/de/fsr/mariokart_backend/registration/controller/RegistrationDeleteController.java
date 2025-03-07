package de.fsr.mariokart_backend.registration.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.service.RegistrationDeleteService;
import lombok.AllArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/teams")
@AllArgsConstructor
public class RegistrationDeleteController {

    private final RegistrationDeleteService registrationDeleteService;

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        try {
            registrationDeleteService.deleteTeam(id);
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @DeleteMapping
    public void deleteAllTeams() {
        try {
            registrationDeleteService.deleteAllTeams();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}