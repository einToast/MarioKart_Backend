package de.fsr.mariokart_backend.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        try {
            return ResponseEntity.ok(surveyCreateService.createSurvey(question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerReturnDTO> submitAnswer(@RequestBody AnswerInputDTO answer) {
        try {
            return ResponseEntity.ok(surveyCreateService.submitAnswer(answer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 