package de.fsr.mariokart_backend.healthcheck.controller;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.SettingsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/healthcheck")
@AllArgsConstructor
public class HealthcheckController {
    @GetMapping
    public ResponseEntity<String> getHealthcheck() {
        return ResponseEntity.ok("OK");
    }

}
