package de.fsr.mariokart_backend.user.exception;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;

public class TokenNotFoundException extends EntityNotFoundException {
    public TokenNotFoundException() {
        super();
    }

    public TokenNotFoundException(String message) {
        super(message);
    }

    public TokenNotFoundException(Throwable cause) {
        super(cause);
    }
}