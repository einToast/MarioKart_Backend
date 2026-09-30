package de.fsr.mariokart_backend.survey.controller.pub;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;

import de.fsr.mariokart_backend.config.CookieProperties;
import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.SurveyKeyRequiredException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerSubmissionResult;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.SURVEY)
public class PublicSurveyCreateController {

    public static final String SURVEY_KEY_COOKIE_NAME = "surveyKey";
    private static final Duration SURVEY_KEY_COOKIE_MAX_AGE = Duration.ofDays(2);

    private final PublicSurveyCreateService publicSurveyCreateService;
    private final CookieProperties cookieProperties;

    @PostMapping("/answer")
    public ResponseEntity<AnswerReturnDTO> submitAnswer(
            @RequestBody AnswerInputDTO answer,
            @CookieValue(value = "user", required = false) String userJson,
            @CookieValue(value = SURVEY_KEY_COOKIE_NAME, required = false) String surveyKey) {
        try {
            AnswerSubmissionResult result = publicSurveyCreateService.submitAnswer(answer, userJson, surveyKey);
            ResponseEntity.BodyBuilder response = ResponseEntity.ok();
            if (result.issuedSurveyKey() != null) {
                response.header(HttpHeaders.SET_COOKIE, buildSurveyKeyCookie(result.issuedSurveyKey()).toString());
            }
            return response.body(result.answer());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (SurveyKeyRequiredException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseCookie buildSurveyKeyCookie(String surveyKey) {
        return ResponseCookie.from(SURVEY_KEY_COOKIE_NAME, surveyKey)
                .path(cookieProperties.getPath())
                .secure(cookieProperties.isSecure())
                .httpOnly(true)
                .sameSite(cookieProperties.getSameSite())
                .maxAge(SURVEY_KEY_COOKIE_MAX_AGE)
                .build();
    }
}
