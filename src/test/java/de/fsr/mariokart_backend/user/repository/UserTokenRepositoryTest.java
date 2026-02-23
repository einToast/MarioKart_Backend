package de.fsr.mariokart_backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;
import de.fsr.mariokart_backend.user.model.User;
import de.fsr.mariokart_backend.user.model.UserToken;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class UserTokenRepositoryTest extends PostgresTestBase {

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByTokenWorks() {
        User user = new User("player1", false);
        user.setPassword("secret");
        userRepository.save(user);

        UserToken userToken = new UserToken(user, LocalDateTime.now().plusHours(8));
        UserToken savedToken = userTokenRepository.save(userToken);

        assertThat(savedToken.getToken()).isNotNull();
        assertThat(userTokenRepository.findByToken(savedToken.getToken())).contains(savedToken);
        assertThat(userTokenRepository.findByToken(UUID.randomUUID())).isEmpty();
    }
}
