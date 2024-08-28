package de.fsr.mariokart_backend.exception;

public class RoundsAlreadyExistsException extends Exception {
    public RoundsAlreadyExistsException() {
        super();
    }

    public RoundsAlreadyExistsException(String message) {
        super(message);
    }

    public RoundsAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
