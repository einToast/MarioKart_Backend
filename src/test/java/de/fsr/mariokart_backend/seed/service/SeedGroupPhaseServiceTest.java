package de.fsr.mariokart_backend.seed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SeedGroupPhaseServiceTest {

    @Mock
    private RoundRepository roundRepository;
    @Mock
    private BreakRepository breakRepository;
    @Mock
    private PointsRepository pointsRepository;
    @Mock
    private PublicSettingsReadService publicSettingsReadService;

    @InjectMocks
    private SeedGroupPhaseService service;

    private final AtomicLong ids = new AtomicLong(1L);
    private Round first;
    private Round second;
    private Round third;
    private Round finalRound;
    private Break aBreak;

    @BeforeEach
    void setUp() {
        first = round(1, false);
        second = round(2, false);
        third = round(3, false);
        finalRound = round(4, true);
        aBreak = new Break();
        aBreak.setStartTime(LocalDateTime.now().plusHours(2));
        aBreak.setEndTime(aBreak.getStartTime().plusMinutes(30));
        second.setBreakTime(aBreak);

        TournamentDTO settings = new TournamentDTO();
        settings.setRoundMinutes(25);
        when(publicSettingsReadService.getSettings()).thenReturn(settings);
        when(roundRepository.findAll()).thenReturn(List.of(third, finalRound, first, second));
    }

    @Test
    void playsEveryGroupRoundAndTheBreakSoThatTheyEndNow() {
        service.playGroupPhase();

        assertThat(List.of(first, second, third)).allMatch(Round::isPlayed);
        assertThat(finalRound.isPlayed()).isFalse();
        assertThat(aBreak.isBreakEnded()).isTrue();
        assertThat(aBreak.getStartTime()).isEqualTo(first.getEndTime());
        assertThat(Duration.between(aBreak.getStartTime(), aBreak.getEndTime())).isEqualTo(Duration.ofMinutes(30));
        assertThat(second.getStartTime()).isEqualTo(aBreak.getEndTime());
        assertThat(third.getStartTime()).isEqualTo(second.getEndTime());
        assertThat(List.of(first, second, third)).allMatch(round ->
                Duration.between(round.getStartTime(), round.getEndTime()).equals(Duration.ofMinutes(25)));
        assertThat(third.getEndTime()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.MINUTES));
    }

    @Test
    void entersThePointsOfFourRacesForEveryTeamOfTheGroupGames() {
        service.playGroupPhase();

        int minimum = SeedGroupPhaseService.RACES_PER_GAME * SeedGroupPhaseService.RACE_POINTS[11];
        int maximum = SeedGroupPhaseService.RACES_PER_GAME * SeedGroupPhaseService.RACE_POINTS[0];
        assertThat(pointsOf(first, second, third)).allMatch(points ->
                points.getGroupPoints() >= minimum && points.getGroupPoints() <= maximum);
        assertThat(pointsOf(first, second, third).stream().map(Points::getGroupPoints).distinct().count())
                .isGreaterThan(1);
        assertThat(pointsOf(finalRound)).allMatch(points -> points.getGroupPoints() == 0);
    }

    @Test
    void entersTheSamePointsEveryTime() {
        service.playGroupPhase();
        List<Integer> firstRun = pointsOf(first, second, third).stream().map(Points::getGroupPoints).toList();

        setUp();
        service.playGroupPhase();

        assertThat(pointsOf(first, second, third)).extracting(Points::getGroupPoints).isEqualTo(firstRun);
    }

    private List<Points> pointsOf(Round... rounds) {
        return Arrays.stream(rounds)
                .flatMap(round -> round.getGames().stream())
                .sorted(Comparator.comparing(Game::getId))
                .flatMap(game -> game.getPoints().stream().sorted(Comparator.comparing(Points::getId)))
                .toList();
    }

    private Round round(int roundNumber, boolean finalGame) {
        Round round = new Round();
        round.setId(ids.getAndIncrement());
        round.setRoundNumber(roundNumber);
        round.setFinalGame(finalGame);
        round.setStartTime(LocalDateTime.now().plusHours(roundNumber));
        round.setEndTime(round.getStartTime().plusMinutes(20));
        Set<Game> games = new HashSet<>();
        for (int switchIndex = 0; switchIndex < 2; switchIndex++) {
            Game game = new Game();
            game.setId(ids.getAndIncrement());
            game.setSwitchIndex(switchIndex);
            game.setRound(round);
            Set<Points> points = new HashSet<>();
            for (int team = 0; team < 4; team++) {
                Points teamPoints = new Points();
                teamPoints.setId(ids.getAndIncrement());
                teamPoints.setGame(game);
                points.add(teamPoints);
            }
            game.setPoints(points);
            games.add(game);
        }
        round.setGames(games);
        return round;
    }
}
