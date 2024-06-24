package de.fsr.mariokart_backend.user.service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.user.UserProperties;
import de.fsr.mariokart_backend.user.exception.PasswordMismatchException;
import de.fsr.mariokart_backend.user.exception.TokenExpiredException;
import de.fsr.mariokart_backend.user.exception.TokenNotFoundException;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationRequestDTO;
import de.fsr.mariokart_backend.user.model.dto.AuthenticationResponseDTO;
import de.fsr.mariokart_backend.user.model.dto.UserCreationDTO;
import de.fsr.mariokart_backend.user.model.dto.UserPasswordsDTO;
import de.fsr.mariokart_backend.user.repository.UserRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JWTManagerService jwtManagerService;
    private final UserTokenService userTokenService;
    private final UserRepository userRepository;
    private final UserProperties userProperties;

    public AuthenticationResponseDTO authenticateUser(AuthenticationRequestDTO request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        User user = (User) authentication.getPrincipal();
        String accessToken = jwtManagerService.generateJWT(user);

        return new AuthenticationResponseDTO(accessToken, user);
    }

    public User registerUser(UserPasswordsDTO userPasswords, String token) throws TokenNotFoundException, TokenExpiredException, PasswordMismatchException {
        try {
            UUID uuid = UUID.fromString(token);
            UserToken userToken = userTokenService.getUserToken(uuid);

            if (LocalDateTime.now().isAfter(userToken.getExpiresAt())) {
                throw new TokenExpiredException("The token is expired.");
            }

            if (!userPasswords.getPassword().equals(userPasswords.getPasswordConfirm())) {
                throw new PasswordMismatchException("The passwords do not match.");
            }

            User user = userToken.getUser();
            user.setPassword(userPasswords.getPassword());

            return userRepository.save(user);
        } catch (EntityNotFoundException e) {
            throw new TokenNotFoundException(e);
        }
    }

    public List<UserToken> createUsers(List<UserCreationDTO> userCreations) throws EntityNotFoundException {
        List<User> users = new LinkedList<>();
        List<UserToken> userTokens = new LinkedList<>();

        for(UserCreationDTO userCreation: userCreations){
            User user = new User(userCreation.getUsername(), userCreation.isAdmin());
            users.add(user);

            userTokens.add(userTokenService.buildUserToken(user));

        }

        userRepository.saveAll(users);
        userTokenService.saveUserTokens(userTokens);

        return userTokens;
    }
}