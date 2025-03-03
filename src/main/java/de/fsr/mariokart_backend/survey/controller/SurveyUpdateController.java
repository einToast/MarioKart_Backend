package de.fsr.mariokart_backend.survey.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.SurveyUpdateService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/survey")
@AllArgsConstructor
public class SurveyUpdateController {

    private final SurveyUpdateService surveyUpdateService;

    @PutMapping("/{id}")
    public ResponseEntity<QuestionReturnDTO> updateQuestion(@PathVariable Long id,
            @RequestBody QuestionInputDTO question) {
        try {
            return ResponseEntity.ok(surveyUpdateService.updateQuestion(id, question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 