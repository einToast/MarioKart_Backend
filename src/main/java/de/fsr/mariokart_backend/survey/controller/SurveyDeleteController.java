package de.fsr.mariokart_backend.survey.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.survey.service.SurveyDeleteService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/survey")
@AllArgsConstructor
public class SurveyDeleteController {

    private final SurveyDeleteService surveyDeleteService;

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        surveyDeleteService.deleteQuestion(id);
    }

    @DeleteMapping
    public void deleteAllQuestions() {
        surveyDeleteService.deleteAllQuestions();
    }
} 