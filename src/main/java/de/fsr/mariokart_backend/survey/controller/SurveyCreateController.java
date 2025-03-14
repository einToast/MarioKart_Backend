package de.fsr.mariokart_backend.survey.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.SurveyCreateService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/survey")
@AllArgsConstructor
public class SurveyCreateController {

    private final SurveyCreateService surveyCreateService;

    @PostMapping
    public ResponseEntity<QuestionReturnDTO> createSurvey(@RequestBody QuestionInputDTO question) {
        return ResponseEntity.ok(surveyCreateService.createSurvey(question));
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerReturnDTO> submitAnswer(
            @RequestBody AnswerInputDTO answer,
            @RequestHeader("X-Team-ID") Long teamId) {
        try {
            return ResponseEntity.ok(surveyCreateService.submitAnswer(answer, teamId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}