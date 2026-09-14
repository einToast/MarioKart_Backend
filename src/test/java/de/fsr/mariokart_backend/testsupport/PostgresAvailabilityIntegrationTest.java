package de.fsr.mariokart_backend.testsupport;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class PostgresAvailabilityIntegrationTest extends PostgresTestBase {

    @Autowired
    private Environment environment;

    @Test
    void testsRunAgainstPostgres() {
        assertThat(environment.getProperty("test.database.provider"))
                .as("Expected integration tests to use PostgreSQL Testcontainers, but fallback H2 was used.")
                .isEqualTo("postgres");
    }
}
