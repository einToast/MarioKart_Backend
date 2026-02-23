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

import de.fsr.mariokart_backend.registration.model.Character;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.CharacterRepository;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;
import de.fsr.mariokart_backend.testsupport.TestDataFactory;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class PointsRepositoryTest extends PostgresTestBase {

    @Autowired
    private PointsRepository pointsRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private RoundRepository roundRepository;

    @Autowired
    private GameRepository gameRepository;

    @Test
    void findByGameIdAndTeamIdAndByGameIdWork() {
        Character charOne = TestDataFactory.character("Mario");
        Character charTwo = TestDataFactory.character("Luigi");
        characterRepository.save(charOne);
        characterRepository.save(charTwo);

        Team teamOne = TestDataFactory.team("Alpha", charOne);
        Team teamTwo = TestDataFactory.team("Beta", charTwo);
        charOne.setTeam(teamOne);
        charTwo.setTeam(teamTwo);
        teamRepository.save(teamOne);
        teamRepository.save(teamTwo);

        Round round = new Round();
        round.setRoundNumber(1);
        roundRepository.save(round);

        Game game = new Game();
        game.setRound(round);
        game.setSwitchGame("Blue");
        gameRepository.save(game);

        Points first = new Points();
        first.setGame(game);
        first.setTeam(teamOne);
        first.setGroupPoints(12);
        first.setFinalPoints(0);

        Points second = new Points();
        second.setGame(game);
        second.setTeam(teamTwo);
        second.setGroupPoints(8);
        second.setFinalPoints(0);

        pointsRepository.save(first);
        pointsRepository.save(second);

        assertThat(pointsRepository.findByGameId(game.getId())).hasSize(2);
        assertThat(pointsRepository.findByGameIdAndTeamId(game.getId(), teamOne.getId())).contains(first);
        assertThat(pointsRepository.findByGameIdAndTeamId(game.getId(), 999L)).isEmpty();
    }
}
