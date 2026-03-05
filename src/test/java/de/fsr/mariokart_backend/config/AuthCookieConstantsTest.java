package de.fsr.mariokart_backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class AuthCookieConstantsTest {

    @Test
    void authCookieNameConstantHasExpectedValue() {
        assertThat(AuthCookieConstants.AUTH_COOKIE_NAME).isEqualTo("authToken");
        assertThat(new AuthCookieConstants()).isNotNull();
    }
}
