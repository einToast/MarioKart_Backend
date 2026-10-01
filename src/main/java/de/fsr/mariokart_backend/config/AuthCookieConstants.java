package de.fsr.mariokart_backend.config;

import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)

public final class AuthCookieConstants {
    public static final String AUTH_COOKIE_NAME = "authToken";
}
