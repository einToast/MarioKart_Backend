package de.fsr.mariokart_backend.survey.model.dto;

import java.time.Duration;

public record AnswerSubmissionResult(AnswerReturnDTO answer, String issuedSurveyKey, Duration surveyKeyMaxAge) {
}
