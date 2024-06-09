package de.fsr.mariokart_backend.exception;

import java.util.function.Supplier;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
