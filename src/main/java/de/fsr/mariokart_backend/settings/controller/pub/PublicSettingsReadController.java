package de.fsr.mariokart_backend.settings.controller.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.SETTINGS)
public class PublicSettingsReadController {

    private final PublicSettingsReadService publicSettingsReadService;

    @GetMapping
    public ResponseEntity<TournamentDTO> getSettings() {
        try {
            return ResponseEntity.ok(publicSettingsReadService.getSettings());
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}