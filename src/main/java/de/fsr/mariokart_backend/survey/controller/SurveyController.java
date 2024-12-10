package de.fsr.mariokart_backend.survey.controller;

import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.SurveyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/survey")
@AllArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @GetMapping()
    public List<QuestionReturnDTO> getQuestions() {
        return surveyService.getQuestions();
    }

    @GetMapping("/active")
    public List<QuestionReturnDTO> getActiveQuestions() {
        return surveyService.getActiveQuestions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionReturnDTO> getQuestion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(surveyService.getQuestion(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/answers")
    public List<AnswerReturnDTO> getAnswersOfQuestion(@PathVariable Long id) {
        return surveyService.getAnswersOfQuestion(id);
    }

    @PostMapping
    public ResponseEntity<QuestionReturnDTO> createSurvey(@RequestBody QuestionInputDTO question) {
        try {
            return ResponseEntity.ok(surveyService.createSurvey(question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/answer")
    public ResponseEntity<AnswerReturnDTO> submitAnswer(@RequestBody AnswerInputDTO answer) {
        try {
            return ResponseEntity.ok(surveyService.submitAnswer(answer));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionReturnDTO> updateQuestion(@PathVariable Long id, @RequestBody QuestionInputDTO question) {
        try {
            return ResponseEntity.ok(surveyService.updateQuestion(id, question));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        surveyService.deleteQuestion(id);
    }

}
