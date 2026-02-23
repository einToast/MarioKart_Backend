package de.fsr.mariokart_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProperties userProperties;

    @InjectMocks
    private UserService service;

    @Test
    void getUserThrowsWhenUnknownUsername() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser("ghost"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("username");
    }

    @Test
    void createAndRegisterIfNotExistReturnsExistingUser() {
        User existing = new User("admin", true);
        when(userRepository.getUserByUsername("admin")).thenReturn(Optional.of(existing));

        User result = service.createAndRegisterIfNotExist(new User("admin", true));

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }
}
