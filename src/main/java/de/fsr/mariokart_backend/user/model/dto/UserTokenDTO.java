package de.fsr.mariokart_backend.user.model.dto;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.fsr.mariokart_backend.user.model.UserToken;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTokenDTO {
    private UUID token;

    private LocalDateTime expiresAt;

    private UserDTO user;

    public UserTokenDTO(UserToken userToken) {
        setToken(userToken.getToken());
        setExpiresAt(userToken.getExpiresAt());
        setUser(new UserDTO(userToken.getUser()));
    }

    public static List<UserTokenDTO> fromUserTokenList(List<UserToken> userTokens) {
        List<UserTokenDTO> userTokenDTOS = new LinkedList<>();

        userTokens.forEach(userToken -> userTokenDTOS.add(new UserTokenDTO(userToken)));

        return userTokenDTOS;
    }
}
