package de.fsr.mariokart_backend.ocr.model.dto;

import java.util.List;

public record OcrReturnDTO(
        List<OcrCharacterDTO> characters) {
}
