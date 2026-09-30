package de.fsr.mariokart_backend.exception;

public class SurveyKeyRequiredException extends Exception {
    public SurveyKeyRequiredException() {
        super();
    }

    public SurveyKeyRequiredException(String message) {
        super(message);
    }

    public SurveyKeyRequiredException(Throwable cause) {
        super(cause);
    }
}
