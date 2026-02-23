package de.fsr.mariokart_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;
import de.fsr.mariokart_backend.user.repository.UserTokenRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserTokenServiceTest {

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private UserProperties userProperties;

    @InjectMocks
    private UserTokenService service;

    @Test
    void buildUserTokenUsesConfiguredExpiry() {
        when(userProperties.getExpiresAfter()).thenReturn(8);

        LocalDateTime before = LocalDateTime.now().plusHours(7);
        UserToken token = service.buildUserToken(new User("user", false));
        LocalDateTime after = LocalDateTime.now().plusHours(9);

        assertThat(token.getExpiresAt()).isAfter(before);
        assertThat(token.getExpiresAt()).isBefore(after);
    }

    @Test
    void getUserTokenThrowsWhenUnknown() {
        UUID tokenId = UUID.randomUUID();
        when(userTokenRepository.findByToken(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserToken(tokenId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("uuid");
    }
}
