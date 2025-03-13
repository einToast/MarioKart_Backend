package de.fsr.mariokart_backend.settings.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.service.SettingsDeleteService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/settings")
@AllArgsConstructor
public class SettingsDeleteController {

    private final SettingsDeleteService settingsDeleteService;

    @DeleteMapping("/reset")
    public void reset() {
        try {
            settingsDeleteService.reset();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}