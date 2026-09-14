package de.fsr.mariokart_backend.testsupport;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresTestBase.class);
    private static final String REQUIRE_POSTGRES_PROPERTY = "test.require.postgres";
    private static final String FORCE_H2_PROPERTY = "test.force.h2";
    private static final boolean REQUIRE_POSTGRES = Boolean.parseBoolean(System.getProperty(REQUIRE_POSTGRES_PROPERTY, "false"));
    private static final boolean FORCE_H2 = Boolean.parseBoolean(System.getProperty(FORCE_H2_PROPERTY, "false"));
    private static final String H2_FALLBACK_DB = "pg_fallback_" + UUID.randomUUID().toString().replace("-", "");

    private static final PostgreSQLContainer<?> POSTGRES;
    private static final boolean POSTGRES_AVAILABLE;
    private static final RuntimeException POSTGRES_STARTUP_EXCEPTION;

    static {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        boolean available = false;
        RuntimeException startupException = null;

        if (FORCE_H2) {
            LOG.info("Forcing H2 for integration repository tests via -D{}=true.", FORCE_H2_PROPERTY);
        } else {
            try {
                postgres.start();
                available = true;
                LOG.info("Using Testcontainers PostgreSQL for integration repository tests.");
            } catch (RuntimeException ex) {
                startupException = ex;
                LOG.warn("PostgreSQL container unavailable, falling back to H2. Cause: {}", ex.getMessage());
            }
        }

        POSTGRES = postgres;
        POSTGRES_AVAILABLE = available;
        POSTGRES_STARTUP_EXCEPTION = startupException;

        if (FORCE_H2 && REQUIRE_POSTGRES) {
            throw new IllegalStateException(
                    "Conflicting test flags: -D" + FORCE_H2_PROPERTY + "=true and -D"
                            + REQUIRE_POSTGRES_PROPERTY + "=true.");
        }

        if (!POSTGRES_AVAILABLE && REQUIRE_POSTGRES) {
            throw new IllegalStateException(
                    "PostgreSQL Testcontainer could not be started and strict mode is enabled via -D"
                            + REQUIRE_POSTGRES_PROPERTY + "=true.",
                    POSTGRES_STARTUP_EXCEPTION);
        }
    }

    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        if (POSTGRES_AVAILABLE) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
            registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        } else {
            registry.add("spring.datasource.url",
                    () -> "jdbc:h2:mem:" + H2_FALLBACK_DB + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            registry.add("spring.datasource.username", () -> "test_user");
            registry.add("spring.datasource.password", () -> "test_password");
            registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
            registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        }

        registry.add("test.database.provider", () -> POSTGRES_AVAILABLE ? "postgres" : "h2");
        // Avoid shutdown hangs in forked test JVMs when schema drop runs after container teardown.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    }
}
