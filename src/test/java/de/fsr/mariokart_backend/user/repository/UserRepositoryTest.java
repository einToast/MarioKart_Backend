package de.fsr.mariokart_backend.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

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

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class UserRepositoryTest extends PostgresTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void usernameQueriesWork() {
        User user = new User("admin", true);
        user.setPassword("secret");
        userRepository.save(user);

        assertThat(userRepository.findByUsername("admin")).contains(user);
        assertThat(userRepository.getUserByUsername("admin")).contains(user);
        assertThat(userRepository.existsByUsername("admin")).isTrue();
        assertThat(userRepository.findByUsername("ghost")).isEmpty();
    }
}
