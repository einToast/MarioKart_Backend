package de.fsr.mariokart_backend.survey.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyReadService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SURVEY)
public class AdminSurveyReadController {

    private final AdminSurveyReadService adminSurveyReadService;

    @GetMapping()
    public List<QuestionReturnDTO> getQuestions() {
        return adminSurveyReadService.getQuestions();
    }

    @GetMapping("/{id}/answers")
    public List<AnswerReturnDTO> getAnswersOfQuestion(@PathVariable Long id) {
        try {
            return adminSurveyReadService.getAnswersOfQuestion(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/statistics")
    public List<Integer> getStatisticsOfQuestion(@PathVariable Long id) {
        try {
            return adminSurveyReadService.getStatisticsOfQuestion(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}/answers/count")
    public Integer getNumberOfAnswers(@PathVariable Long id) {
        return adminSurveyReadService.getNumberOfAnswers(id);
    }
    

}