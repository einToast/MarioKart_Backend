package de.fsr.mariokart_backend.schedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class BreakRepositoryTest extends PostgresTestBase {

    @Autowired
    private BreakRepository breakRepository;

    @Test
    void saveAndReadBreakWorks() {
        Break aBreak = new Break();
        aBreak.setStartTime(LocalDateTime.now());
        aBreak.setEndTime(LocalDateTime.now().plusMinutes(10));
        aBreak.setBreakEnded(false);

        Break saved = breakRepository.save(aBreak);

        assertThat(saved.getId()).isNotNull();
        assertThat(breakRepository.findById(saved.getId())).contains(saved);
    }
}
