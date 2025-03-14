package de.fsr.mariokart_backend.survey.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.SurveyReadService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/survey")
@AllArgsConstructor
public class SurveyReadController {

    private final SurveyReadService surveyReadService;

    @GetMapping()
    public List<QuestionReturnDTO> getQuestions() {
        return surveyReadService.getQuestions();
    }

    @GetMapping("/visible")
    public List<QuestionReturnDTO> getVisibleQuestions() {
        return surveyReadService.getVisibleQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionReturnDTO> getQuestion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(surveyReadService.getQuestion(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/answers")
    public List<AnswerReturnDTO> getAnswersOfQuestion(@PathVariable Long id) {
        return surveyReadService.getAnswersOfQuestion(id);
    }
} 