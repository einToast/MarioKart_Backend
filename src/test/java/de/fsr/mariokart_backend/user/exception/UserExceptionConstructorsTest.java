package de.fsr.mariokart_backend.user.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class UserExceptionConstructorsTest {

    @Test
    void userExceptionsPreserveMessageAndCause() {
        RuntimeException cause = new RuntimeException("cause");

        assertThat(new PasswordMismatchException("pw mismatch")).hasMessage("pw mismatch");
        assertThat(new PasswordMismatchException(cause)).hasCause(cause);

        assertThat(new TokenNotFoundException("token missing")).hasMessage("token missing");
        assertThat(new TokenNotFoundException(cause)).hasCause(cause);

        assertThat(new TokenExpiredException("expired")).hasMessage("expired");
    }
}
