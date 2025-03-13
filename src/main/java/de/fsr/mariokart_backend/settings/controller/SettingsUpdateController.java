package de.fsr.mariokart_backend.settings.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsUpdateService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/settings")
@AllArgsConstructor
public class SettingsUpdateController {

    private final SettingsUpdateService settingsUpdateService;

    @PutMapping
    @ResponseBody
    public ResponseEntity<TournamentDTO> updateSettings(@RequestBody TournamentDTO tournamentDTO) {
        try {
            return ResponseEntity.ok(settingsUpdateService.updateSettings(tournamentDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (RoundsAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}