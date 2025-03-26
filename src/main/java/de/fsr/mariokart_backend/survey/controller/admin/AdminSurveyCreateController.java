package de.fsr.mariokart_backend.survey.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SURVEY)
public class AdminSurveyCreateController {

    private final AdminSurveyCreateService publicSurveyCreateService;

    @PostMapping
    public ResponseEntity<QuestionReturnDTO> createQuestion(@RequestBody QuestionInputDTO question) {
        return ResponseEntity.ok(publicSurveyCreateService.createQuestion(question));
    }
}