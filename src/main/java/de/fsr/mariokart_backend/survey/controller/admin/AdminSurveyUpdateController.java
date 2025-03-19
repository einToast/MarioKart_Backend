package de.fsr.mariokart_backend.survey.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.SURVEY)
public class AdminSurveyUpdateController {

    private final AdminSurveyUpdateService adminSurveyUpdateService;

    @PutMapping("/{id}")
    public ResponseEntity<QuestionReturnDTO> updateQuestion(@PathVariable Long id,
            @RequestBody QuestionInputDTO question) {
        try {
            return ResponseEntity.ok(adminSurveyUpdateService.updateQuestion(id, question));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}