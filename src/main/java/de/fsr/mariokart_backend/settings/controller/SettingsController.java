package de.fsr.mariokart_backend.settings.controller;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.dto.CharacterReturnDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamInputDTO;
import de.fsr.mariokart_backend.registration.model.dto.TeamReturnDTO;
import de.fsr.mariokart_backend.registration.service.RegistrationService;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/settings")
@AllArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<TournamentDTO> getSettings() {
        try{
            return ResponseEntity.ok(settingsService.getSettings());
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<TournamentDTO> updateSettings(@RequestBody TournamentDTO tournamentDTO) {
        try{
            return ResponseEntity.ok(settingsService.updateSettings(tournamentDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
