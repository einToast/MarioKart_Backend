package de.fsr.mariokart_backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class ExceptionConstructorsTest {

    @Test
    void coreExceptionConstructorsExposeMessagesAndCauses() {
        RuntimeException cause = new RuntimeException("cause");

        assertThat(new EntityNotFoundException("missing")).hasMessage("missing");
        assertThat(new EntityNotFoundException(cause)).hasCause(cause);

        assertThat(new NotEnoughTeamsException("few teams")).hasMessage("few teams");
        assertThat(new NotEnoughTeamsException(cause)).hasCause(cause);

        assertThat(new NotificationNotSentException("notify failed")).hasMessage("notify failed");
        assertThat(new NotificationNotSentException(cause)).hasCause(cause);

        assertThat(new RoundsAlreadyExistsException("exists")).hasMessage("exists");
        assertThat(new RoundsAlreadyExistsException(cause)).hasCause(cause);
    }
}
