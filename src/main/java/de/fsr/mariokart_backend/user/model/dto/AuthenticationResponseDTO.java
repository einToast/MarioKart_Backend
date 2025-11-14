package de.fsr.mariokart_backend.user.model.dto;

import de.fsr.mariokart_backend.user.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticationResponseDTO {
    private UserDTO user;

    public AuthenticationResponseDTO(User user) {
        setUser(new UserDTO(user));
    }

}
