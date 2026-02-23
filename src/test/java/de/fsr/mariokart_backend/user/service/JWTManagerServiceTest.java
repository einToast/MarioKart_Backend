package de.fsr.mariokart_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class JWTManagerServiceTest {

    @Mock
    private UserProperties userProperties;

    @InjectMocks
    private JWTManagerService service;

    @Test
    void generateAndValidateJwtWorks() {
        String base64Key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        when(userProperties.getSecretKey()).thenReturn(base64Key);
        when(userProperties.getExpiresAfter()).thenReturn(8);

        User user = new User("admin", true);
        String token = service.generateJWT(user);

        assertThat(token).isNotBlank();
        assertThat(service.validateJWT(token)).isTrue();
        assertThat(service.getSubjectFromToken(token)).isEqualTo("admin");
    }

    @Test
    void validateReturnsFalseForInvalidToken() {
        String base64Key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        when(userProperties.getSecretKey()).thenReturn(base64Key);

        assertThat(service.validateJWT("invalid.token.value")).isFalse();
    }
}
