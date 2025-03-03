package de.fsr.mariokart_backend.registration.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void deleteTeam(@PathVariable Long id) throws RoundsAlreadyExistsException, EntityNotFoundException {
        registrationDeleteService.deleteTeam(id);
    }

    @DeleteMapping
    public void deleteAllTeams() throws RoundsAlreadyExistsException {
        registrationDeleteService.deleteAllTeams();
    }
} 