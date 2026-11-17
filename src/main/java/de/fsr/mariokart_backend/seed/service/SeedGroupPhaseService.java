package de.fsr.mariokart_backend.seed.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.service.pub.PublicSettingsReadService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SeedGroupPhaseService {

    static final int[] RACE_POINTS = { 15, 12, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
    static final int RACES_PER_GAME = 4;
    private static final long RANDOM_SEED = 2026L;

    private final RoundRepository roundRepository;
    private final BreakRepository breakRepository;
    private final PointsRepository pointsRepository;
    private final PublicSettingsReadService publicSettingsReadService;

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = { "schedule", "registration" }, allEntries = true)
    public void playGroupPhase() {
        List<Round> rounds = roundRepository.findAll().stream()
                .filter(round -> !round.isFinalGame())
                .sorted(Comparator.comparing(Round::getRoundNumber))
                .toList();
        int roundMinutes = publicSettingsReadService.getSettings().roundMinutesOrDefault();
        long breakMinutes = rounds.stream()
                .map(Round::getBreakTime)
                .filter(Objects::nonNull)
                .mapToLong(SeedGroupPhaseService::minutesOf)
                .sum();

        LocalDateTime time = LocalDateTime.now(ZoneId.systemDefault())
                .minusMinutes((long) roundMinutes * rounds.size() + breakMinutes);
        Random random = new Random(RANDOM_SEED);
        for (Round round : rounds) {
            Break aBreak = round.getBreakTime();
            if (aBreak != null) {
                long minutes = minutesOf(aBreak);
                aBreak.setStartTime(time);
                time = time.plusMinutes(minutes);
                aBreak.setEndTime(time);
                aBreak.setBreakEnded(true);
                breakRepository.save(aBreak);
            }
            round.setStartTime(time);
            time = time.plusMinutes(roundMinutes);
            round.setEndTime(time);
            round.setPlayed(true);
            roundRepository.save(round);

            round.getGames().stream()
                    .sorted(Comparator.comparing(Game::getSwitchIndex))
                    .forEach(game -> playGame(game, random));
        }
    }

    private static long minutesOf(Break aBreak) {
        return Duration.between(aBreak.getStartTime(), aBreak.getEndTime()).toMinutes();
    }

    private void playGame(Game game, Random random) {
        List<Points> points = game.getPoints().stream()
                .sorted(Comparator.comparing(Points::getId))
                .toList();
        List<Integer> places = IntStream.range(0, RACE_POINTS.length).boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        int[] totals = new int[points.size()];
        for (int race = 0; race < RACES_PER_GAME; race++) {
            Collections.shuffle(places, random);
            for (int team = 0; team < points.size(); team++) {
                totals[team] += RACE_POINTS[places.get(team)];
            }
        }
        for (int team = 0; team < points.size(); team++) {
            points.get(team).setGroupPoints(totals[team]);
            pointsRepository.save(points.get(team));
        }
    }
}
