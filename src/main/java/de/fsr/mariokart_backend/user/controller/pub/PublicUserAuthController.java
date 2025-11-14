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

    private static final String AUTH_COOKIE_NAME = "authToken";

    private final AuthenticationService authenticationService;
    private final UserProperties userProperties;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO request) {
        try {
            AuthenticationResult authentication = authenticationService.authenticateUser(request);
            ResponseCookie authCookie = ResponseCookie.from(AUTH_COOKIE_NAME, authentication.getAccessToken())
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("Strict")
                    .maxAge(Duration.ofHours(userProperties.getExpiresAfter()))
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                    .body(authentication.getResponse());
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/login/check")
    public ResponseEntity<AuthenticationResponseDTO> checkLogin(
            @CookieValue(name = AUTH_COOKIE_NAME, required = false) String authCookie) {
        if (authCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AuthenticationResponseDTO response = authenticationService.authenticateUserByToken(authCookie);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteAuthCookie = ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("Strict")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteAuthCookie.toString())
                .build();
    }
}
