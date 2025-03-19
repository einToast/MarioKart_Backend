package de.fsr.mariokart_backend.survey.controller.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyDeleteService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SURVEY)
public class AdminSurveyDeleteController {

    private final AdminSurveyDeleteService publicSurveyDeleteService;

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        publicSurveyDeleteService.deleteQuestion(id);
    }

    @DeleteMapping
    public void deleteAllQuestions() {
        publicSurveyDeleteService.deleteAllQuestions();
    }
}