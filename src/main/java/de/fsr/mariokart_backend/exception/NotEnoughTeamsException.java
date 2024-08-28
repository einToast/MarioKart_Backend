package de.fsr.mariokart_backend.exception;

public class NotEnoughTeamsException extends Exception {
    public NotEnoughTeamsException() {
        super();
    }

    public NotEnoughTeamsException(String message) {
        super(message);
    }

    public NotEnoughTeamsException(Throwable cause) {
        super(cause);
    }
}
