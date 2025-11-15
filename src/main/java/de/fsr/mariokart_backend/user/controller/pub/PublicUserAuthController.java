package de.fsr.mariokart_backend.user.controller.pub;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.config.AuthCookieConstants;
import de.fsr.mariokart_backend.config.CookieProperties;
import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResponseDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResult;
import de.fsr.mariokart_backend.user.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.USER)
public class PublicUserAuthController {

    private final AuthenticationService authenticationService;
    private final UserProperties userProperties;
    private final CookieProperties cookieProperties;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO request) {
        try {
            AuthenticationResult authentication = authenticationService.authenticateUser(request);
            ResponseCookie authCookie = buildAuthCookie(authentication.getAccessToken(),
                    Duration.ofHours(userProperties.getExpiresAfter()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                    .body(authentication.getResponse());
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials", ex);
        }
    }

    @GetMapping("/login/check")
    public ResponseEntity<AuthenticationResponseDTO> checkLogin(
            @CookieValue(name = AuthCookieConstants.AUTH_COOKIE_NAME, required = false) String authCookie) {
        if (authCookie == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        try {
            AuthenticationResponseDTO response = authenticationService.authenticateUserByToken(authCookie);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session", ex);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteAuthCookie = buildAuthCookie("", Duration.ZERO);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAuthCookie.toString())
                .build();
    }

    private ResponseCookie buildAuthCookie(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(AuthCookieConstants.AUTH_COOKIE_NAME, value)
                .path(cookieProperties.getPath())
                .secure(cookieProperties.isSecure())
                .httpOnly(true)
                .sameSite(cookieProperties.getSameSite());

        if (maxAge != null) {
            builder.maxAge(maxAge);
        }

        return builder.build();
    }
}
