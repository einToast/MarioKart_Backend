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

import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class RoundRepositoryTest extends PostgresTestBase {

    @Autowired
    private RoundRepository roundRepository;

    @Test
    void roundQueriesAndDeleteFinalRoundsWork() {
        LocalDateTime base = LocalDateTime.now();

        Round normal = new Round();
        normal.setRoundNumber(1);
        normal.setStartTime(base.minusMinutes(30));
        normal.setEndTime(base.minusMinutes(10));
        normal.setPlayed(false);
        normal.setFinalGame(false);

        Round finalRound = new Round();
        finalRound.setRoundNumber(2);
        finalRound.setStartTime(base.plusMinutes(30));
        finalRound.setEndTime(base.plusMinutes(60));
        finalRound.setPlayed(true);
        finalRound.setFinalGame(true);

        roundRepository.save(normal);
        roundRepository.save(finalRound);

        assertThat(roundRepository.findByFinalGameTrue()).hasSize(1);
        assertThat(roundRepository.findByStartTimeAfter(base)).hasSize(1);
        assertThat(roundRepository.findByStartTimeBefore(base)).hasSize(1);
        assertThat(roundRepository.findByPlayedFalse()).hasSize(1);
        assertThat(roundRepository.findByRoundNumber(1)).contains(normal);
        assertThat(roundRepository.countByPlayedFalse()).isEqualTo(1);

        roundRepository.deleteAllByFinalGameTrue();
        assertThat(roundRepository.findByFinalGameTrue()).isEmpty();
    }
}
