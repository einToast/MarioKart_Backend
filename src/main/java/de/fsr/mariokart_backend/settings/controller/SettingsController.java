package de.fsr.mariokart_backend.settings.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/settings")
@AllArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<TournamentDTO> getSettings() {
        try {
            return ResponseEntity.ok(settingsService.getSettings());
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<TournamentDTO> updateSettings(@RequestBody TournamentDTO tournamentDTO) {
        try {
            return ResponseEntity.ok(settingsService.updateSettings(tournamentDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (RoundsAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/reset")
    public void reset() {
        try {
            settingsService.reset();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

}
