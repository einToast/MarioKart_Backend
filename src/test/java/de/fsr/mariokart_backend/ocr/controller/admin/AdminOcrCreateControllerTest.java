package de.fsr.mariokart_backend.ocr.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.ocr.model.dto.OcrInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.OcrReturnDTO;
import de.fsr.mariokart_backend.ocr.service.admin.AdminOcrCreateService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminOcrCreateControllerTest {

    private static final OcrInputDTO INPUT = new OcrInputDTO("provider", "image");

    @Mock
    private AdminOcrCreateService adminOcrCreateService;

    @InjectMocks
    private AdminOcrCreateController controller;

    @Test
    void detectCharactersReturnsServiceResult() throws Exception {
        OcrReturnDTO expected = new OcrReturnDTO(List.of());
        when(adminOcrCreateService.detectCharactersByRoundAndGame(INPUT, 1L, 2L)).thenReturn(expected);

        assertThat(controller.detectCharactersByRoundAndGame(INPUT, 1L, 2L)).isSameAs(expected);
    }

    @Test
    void detectCharactersMapsMissingGameToNotFound() throws Exception {
        when(adminOcrCreateService.detectCharactersByRoundAndGame(INPUT, 1L, 2L))
                .thenThrow(new EntityNotFoundException("There is no game with this ID in the specified round."));

        assertThatThrownBy(() -> controller.detectCharactersByRoundAndGame(INPUT, 1L, 2L))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getReason()).isEqualTo("There is no game with this ID in the specified round.");
                });
    }

    @Test
    void detectCharactersMapsOcrFailureToInternalServerError() throws Exception {
        when(adminOcrCreateService.detectCharactersByRoundAndGame(INPUT, 1L, 2L))
                .thenThrow(new RuntimeException("Failed to send request to ocr service"));

        assertThatThrownBy(() -> controller.detectCharactersByRoundAndGame(INPUT, 1L, 2L))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(e.getReason()).isEqualTo("Failed to detect characters");
                });
    }
}
