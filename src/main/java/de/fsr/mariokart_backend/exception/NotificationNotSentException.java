package de.fsr.mariokart_backend.exception;

public class NotificationNotSentException extends Exception {
    public NotificationNotSentException() {
        super();
    }

    public NotificationNotSentException(String message) {
        super(message);
    }

    public NotificationNotSentException(Throwable cause) {
        super(cause);
    }
}
