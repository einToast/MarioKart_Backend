package de.fsr.mariokart_backend.ocr.controller.admin;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.ocr.model.dto.OcrInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrReturnDTO;
import de.fsr.mariokart_backend.ocr.service.admin.AdminOcrCreateService;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.OCR)
public class AdminOcrCreateController {

    private final AdminOcrCreateService adminOcrCreateService;

    @PostMapping("{roundId}/{gameId}")
    public OcrReturnDTO detectCharactersByRoundAndGame(@RequestBody OcrInputDTO ocrInputDTO, @PathVariable Long roundId,
            @PathVariable Long gameId) {
        // TODO: better error handling
        try {
            return adminOcrCreateService.detectCharactersByRoundAndGame(ocrInputDTO, roundId, gameId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to detect characters");
        }
    }

}
