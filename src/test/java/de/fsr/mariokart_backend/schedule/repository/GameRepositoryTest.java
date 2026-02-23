package de.fsr.mariokart_backend.schedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class GameRepositoryTest extends PostgresTestBase {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Test
    void findByRoundIdWorks() {
        Round roundOne = new Round();
        roundOne.setRoundNumber(1);

        Round roundTwo = new Round();
        roundTwo.setRoundNumber(2);

        roundRepository.save(roundOne);
        roundRepository.save(roundTwo);

        Game first = new Game();
        first.setSwitchGame("Blue");
        first.setRound(roundOne);

        Game second = new Game();
        second.setSwitchGame("Red");
        second.setRound(roundOne);

        Game other = new Game();
        other.setSwitchGame("Green");
        other.setRound(roundTwo);

        gameRepository.save(first);
        gameRepository.save(second);
        gameRepository.save(other);

        assertThat(gameRepository.findByRoundId(roundOne.getId())).hasSize(2);
        assertThat(gameRepository.findByRoundId(9999L)).isEmpty();
    }
}
