package de.fsr.mariokart_backend.exception;

public class AnswerLimitReachedException extends Exception {
    public AnswerLimitReachedException() {
        super();
    }

    public AnswerLimitReachedException(String message) {
        super(message);
    }

    public AnswerLimitReachedException(Throwable cause) {
        super(cause);
    }
}
