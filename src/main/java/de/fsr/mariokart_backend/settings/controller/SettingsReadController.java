package de.fsr.mariokart_backend.settings.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsReadService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/settings")
@AllArgsConstructor
public class SettingsReadController {

    private final SettingsReadService settingsReadService;

    @GetMapping
    public ResponseEntity<TournamentDTO> getSettings() {
        try {
            return ResponseEntity.ok(settingsReadService.getSettings());
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}