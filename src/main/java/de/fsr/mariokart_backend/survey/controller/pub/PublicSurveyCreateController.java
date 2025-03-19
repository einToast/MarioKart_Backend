package de.fsr.mariokart_backend.survey.controller.pub;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.SURVEY)
public class PublicSurveyCreateController {

    private final PublicSurveyCreateService publicSurveyCreateService;

    @PostMapping("/answer")
    public ResponseEntity<AnswerReturnDTO> submitAnswer(
            @RequestBody AnswerInputDTO answer,
            @RequestHeader("X-Team-ID") Long teamId) {
        try {
            return ResponseEntity.ok(publicSurveyCreateService.submitAnswer(answer, teamId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}