package de.fsr.mariokart_backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.exception.PasswordMismatchException;
import de.fsr.mariokart_backend.user.exception.TokenExpiredException;
import de.fsr.mariokart_backend.user.exception.TokenNotFoundException;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResult;
import de.fsr.mariokart_backend.user.model.dto.UserCreationDTO;
import de.fsr.mariokart_backend.user.model.dto.UserPasswordsDTO;
import de.fsr.mariokart_backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTManagerService jwtManagerService;

    @Mock
    private UserTokenService userTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void authenticateUserReturnsTokenAndResponse() {
        User user = new User("admin", true);
        user.setID(1);
        AuthenticationRequestDTO request = new AuthenticationRequestDTO("admin", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtManagerService.generateJWT(user)).thenReturn("jwt-token");

        AuthenticationResult result = service.authenticateUser(request);

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.getResponse().getUser().getUsername()).isEqualTo("admin");
    }

    @Test
    void authenticateUserByTokenThrowsForInvalidToken() {
        when(jwtManagerService.validateJWT("bad")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticateUserByToken("bad"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void registerUserThrowsForPasswordMismatch() throws Exception {
        UUID tokenId = UUID.randomUUID();
        UserToken userToken = new UserToken(new User("new-user", false), LocalDateTime.now().plusHours(1));
        when(userTokenService.getUserToken(tokenId)).thenReturn(userToken);

        UserPasswordsDTO passwords = new UserPasswordsDTO("a", "b");

        assertThatThrownBy(() -> service.registerUser(passwords, tokenId.toString()))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Test
    void registerUserMapsMissingTokenToTokenNotFound() throws Exception {
        UUID tokenId = UUID.randomUUID();
        when(userTokenService.getUserToken(tokenId)).thenThrow(new EntityNotFoundException("missing"));

        assertThatThrownBy(() -> service.registerUser(new UserPasswordsDTO("a", "a"), tokenId.toString()))
                .isInstanceOf(TokenNotFoundException.class);
    }

    @Test
    void registerUserThrowsForExpiredToken() throws Exception {
        UUID tokenId = UUID.randomUUID();
        UserToken userToken = new UserToken(new User("new-user", false), LocalDateTime.now().minusMinutes(1));
        when(userTokenService.getUserToken(tokenId)).thenReturn(userToken);

        assertThatThrownBy(() -> service.registerUser(new UserPasswordsDTO("a", "a"), tokenId.toString()))
                .isInstanceOf(TokenExpiredException.class);
    }

    @Test
    void createUsersBuildsAndReturnsTokens() throws Exception {
        UserToken built = new UserToken(new User("alpha", false), LocalDateTime.now().plusHours(8));

        when(userTokenService.buildUserToken(any(User.class))).thenReturn(built);
        when(userTokenService.saveUserTokens(any())).thenReturn(List.of(built));

        List<UserToken> result = service.createUsers(List.of(new UserCreationDTO("alpha", false)));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isSameAs(built);
    }

    @Test
    void authenticateUserByTokenReturnsResponseForKnownUser() {
        User user = new User("admin", true);
        user.setID(1);
        when(jwtManagerService.validateJWT("ok")).thenReturn(true);
        when(jwtManagerService.getSubjectFromToken("ok")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThat(service.authenticateUserByToken("ok").getUser().getUsername()).isEqualTo("admin");
    }
}
