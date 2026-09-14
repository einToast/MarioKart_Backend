package de.fsr.mariokart_backend.user.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;

@Tag("unit")
class UserTokenDTOTest {

    @Test
    void constructorMapsTokenExpiryAndUser() {
        User user = new User("admin", true);
        user.setID(5);

        UserToken token = new UserToken();
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setUser(user);

        UserTokenDTO dto = new UserTokenDTO(token);

        assertThat(dto.getToken()).isEqualTo(token.getToken());
        assertThat(dto.getExpiresAt()).isEqualTo(token.getExpiresAt());
        assertThat(dto.getUser().getUsername()).isEqualTo("admin");
    }

    @Test
    void fromUserTokenListMapsEachEntry() {
        User user = new User("player", false);
        user.setID(7);
        UserToken token1 = new UserToken();
        token1.setToken(UUID.randomUUID());
        token1.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token1.setUser(user);

        UserToken token2 = new UserToken();
        token2.setToken(UUID.randomUUID());
        token2.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        token2.setUser(user);

        List<UserTokenDTO> dtos = UserTokenDTO.fromUserTokenList(List.of(token1, token2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(UserTokenDTO::getToken).containsExactly(token1.getToken(), token2.getToken());
    }
}
