package de.fsr.mariokart_backend.survey.controller.pub;

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
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.SURVEY)
public class PublicSurveyReadController {

    private final PublicSurveyReadService publicSurveyReadService;

    @GetMapping("/visible")
    public List<QuestionReturnDTO> getVisibleQuestions() {
        return publicSurveyReadService.getVisibleQuestions();
    }

    @GetMapping("/{id}/statistics")
    public List<Integer> getStatisticsOfQuestion(@PathVariable Long id) {
        try {
            return publicSurveyReadService.getStatisticsOfQuestion(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}