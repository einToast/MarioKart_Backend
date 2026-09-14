package de.fsr.mariokart_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import de.fsr.mariokart_backend.user.model.dto.UpdateUserDTO;
import de.fsr.mariokart_backend.user.model.dto.UserDTO;
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
    void getUserByIdReturnsExistingUser() throws EntityNotFoundException {
        User user = new User("admin", true);
        user.setID(42);
        when(userRepository.findById(42)).thenReturn(Optional.of(user));

        User result = service.getUser(42);

        assertThat(result).isSameAs(user);
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findById(77)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUser(77))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("ID");
    }

    @Test
    void getUsersReturnsRepositoryValues() {
        List<User> users = List.of(new User("admin", true), new User("player", false));
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = service.getUsers();

        assertThat(result).containsExactlyElementsOf(users);
    }

    @Test
    void deleteUserDelegatesToRepository() {
        service.deleteUser(14);

        verify(userRepository).deleteById(14);
    }

    @Test
    void userExistsDelegatesToRepository() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThat(service.userExists("admin")).isTrue();
    }

    @Test
    void createAndRegisterIfNotExistReturnsExistingUser() {
        User existing = new User("admin", true);
        when(userRepository.getUserByUsername("admin")).thenReturn(Optional.of(existing));

        User result = service.createAndRegisterIfNotExist(new User("admin", true));

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createAndRegisterIfNotExistSavesWhenMissing() {
        User newUser = new User("new-user", false);
        when(userRepository.getUserByUsername("new-user")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);

        User result = service.createAndRegisterIfNotExist(newUser);

        assertThat(result).isSameAs(newUser);
        verify(userRepository).save(newUser);
    }

    @Test
    void updateUserReturnsMappedUserDto() throws EntityNotFoundException {
        User user = new User("admin", true);
        user.setID(5);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO dto = service.updateUser(5, new UpdateUserDTO());

        assertThat(dto.getID()).isEqualTo(5);
        assertThat(dto.getUsername()).isEqualTo("admin");
    }
}
