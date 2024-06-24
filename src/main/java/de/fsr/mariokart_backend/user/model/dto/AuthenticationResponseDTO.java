package de.fsr.mariokart_backend.user.model.dto;

import de.fsr.mariokart_backend.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticationResponseDTO {
    private String accessToken;
    private UserDTO user;

    public AuthenticationResponseDTO(String accessToken, User user){
        setAccessToken(accessToken);
        setUser(new UserDTO(user));
    }

}
