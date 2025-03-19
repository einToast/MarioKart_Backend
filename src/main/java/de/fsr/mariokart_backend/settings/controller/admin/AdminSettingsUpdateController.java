package de.fsr.mariokart_backend.settings.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.admin.AdminSettingsUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SETTINGS)
public class AdminSettingsUpdateController {

    private final AdminSettingsUpdateService adminSettingsUpdateService;

    @PutMapping
    @ResponseBody
    public ResponseEntity<TournamentDTO> updateSettings(@RequestBody TournamentDTO tournamentDTO) {
        try {
            return ResponseEntity.ok(adminSettingsUpdateService.updateSettings(tournamentDTO));
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        } catch (RoundsAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}