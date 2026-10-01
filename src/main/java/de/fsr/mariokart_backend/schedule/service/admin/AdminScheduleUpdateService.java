package de.fsr.mariokart_backend.schedule.service.admin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputFullDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.GameRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = {"schedule", "registration"})
@CacheEvict(allEntries = true)
public class AdminScheduleUpdateService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final TeamRepository teamRepository;
    private final AdminScheduleReadService adminScheduleReadService;
    private final PublicScheduleReadService publicScheduleReadService;
    private final ScheduleReturnDTOService scheduleReturnDTOService;
    private final WebSocketService webSocketService;
    private final AdminNotificationCreateService adminNotificationCreateService;

    private static final long PLAY_MINUTES = 20L;
    private static final String ROUND_NOT_FOUND = "There is no round with this ID.";
    private static final String ROUNDS_TOPIC = "/topic/rounds";

    private void updateBreakAndFollowingRounds(Round breakRound, List<Round> roundsAfterBreak) {
        int breakDuration = (int) Duration
                .between(breakRound.getBreakTime().getStartTime(), breakRound.getBreakTime().getEndTime())
                .toMinutes();

        // Calculate the start time for the break based on previous round or current
        // time
        LocalDateTime breakStartTime = calculateBreakStartTime(breakRound);

        // Set the break times
        breakRound.getBreakTime().setStartTime(breakStartTime);
        breakRound.getBreakTime().setEndTime(breakStartTime.plusMinutes(breakDuration));

        // Set the round start time to be after the break
        breakRound.setStartTime(breakRound.getBreakTime().getEndTime());
        breakRound.setEndTime(breakRound.getStartTime().plusMinutes(PLAY_MINUTES));

        // Save changes
        breakRepository.save(breakRound.getBreakTime());
        roundRepository.save(breakRound);

        // Update subsequent rounds
        updateRoundsAfterBreak(roundsAfterBreak, breakRound);
    }

    private LocalDateTime calculateBreakStartTime(Round breakRound) {
        // Find the previous round to use its end time for the break start time
        List<Round> allRounds = roundRepository.findAll();
        allRounds.sort(Comparator.comparing(Round::getRoundNumber));

        // Find the index of the break round
        int breakRoundIndex = -1;
        for (int i = 0; i < allRounds.size(); i++) {
            if (allRounds.get(i).getId().equals(breakRound.getId())) {
                breakRoundIndex = i;
                break;
            }
        }

        if (breakRoundIndex > 0) {
            // Use the end time of the previous round as break start time
            return allRounds.get(breakRoundIndex - 1).getEndTime();
        }
        // Fallback if there's no previous round (shouldn't happen in normal use)
        return LocalDateTime.now(ZoneId.systemDefault());
    }

    private void updateRoundsAfterBreak(List<Round> roundsAfterBreak, Round breakRound) {
        for (int i = 0; i < roundsAfterBreak.size(); i++) {
            Round currentRound = roundsAfterBreak.get(i);
            LocalDateTime startTime = breakRound.getEndTime().plusMinutes(PLAY_MINUTES * i);
            currentRound.setStartTime(startTime);
            currentRound.setEndTime(startTime.plusMinutes(PLAY_MINUTES));
            roundRepository.save(currentRound);
        }
    }

    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation)
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException(ROUND_NOT_FOUND));

        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        notPlayedRounds.sort(Comparator.comparing(Round::getRoundNumber));

        boolean roundPlayedChanged = round.isPlayed() != roundCreation.isPlayed();

        round.setPlayed(roundCreation.isPlayed());

        // Only set endTime to now if the round is being marked as played
        if (roundCreation.isPlayed()) {
            round.setEndTime(LocalDateTime.now(ZoneId.systemDefault()));
        }
        // End time for not played rounds will be set by updateNotPlayedRoundsSchedule

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
        } else if (!round.isPlayed() && !notPlayedRounds.contains(round)) {
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getRoundNumber));
        }

        if (!adminScheduleReadService.isBreakFinished()
                && notPlayedRounds.stream().noneMatch(r -> r.getBreakTime() != null)) {
            throw new RoundsAlreadyExistsException("Break not finished.");
        }

        updateNotPlayedRoundsSchedule(notPlayedRounds);

        if (!adminScheduleReadService.isBreakFinished()) {

            Round breakRound = notPlayedRounds.stream()
                    .filter(r -> r.getBreakTime() != null)
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No round with a break found."));

            List<Round> roundsAfterBreak = notPlayedRounds.stream()
                    .filter(r -> r.getRoundNumber() > breakRound.getRoundNumber())
                    .toList();

            updateBreakAndFollowingRounds(breakRound, roundsAfterBreak);
        }

        Round savedRound = roundRepository.save(round);

        if (roundPlayedChanged) {
            webSocketService.sendMessage(ROUNDS_TOPIC, "update");
            sendNotificationForNextRound();

        }

        return scheduleReturnDTOService.roundToRoundDTO(savedRound);
    }

    public PointsReturnDTO updatePoints(Long roundId, Long gameId, Long teamId, PointsInputDTO pointsCreation)
            throws EntityNotFoundException {
        Points points = pointsRepository.findByGameIdAndTeamId(gameId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("There are no points with this ID."));
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException(ROUND_NOT_FOUND));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("There is no team with this ID."));

        if (round.isFinalGame()) {
            points.setFinalPoints(pointsCreation.getPoints());
        } else {
            points.setGroupPoints(pointsCreation.getPoints());
        }

        points.setGame(game);
        points.setTeam(team);
        return scheduleReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
    }

    public BreakReturnDTO updateBreak(BreakInputDTO breakCreation)
            throws EntityNotFoundException, NotificationNotSentException {
        if (!publicScheduleReadService.isScheduleCreated()) {
            throw new EntityNotFoundException("Schedule not created yet.");
        }

        Break aBreak = breakRepository.findAll().getFirst();
        Round oldRound = aBreak.getRound();
        Round newRound = roundRepository.findById(breakCreation.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException(ROUND_NOT_FOUND));

        // Store old values to check if they've changed
        boolean oldBreakEnded = aBreak.isBreakEnded();
        int newBreakDuration = breakCreation.getBreakDuration();

        boolean breakStatusChanged = breakCreation.getBreakEnded() != null
                && breakCreation.getBreakEnded() != oldBreakEnded;
        if (breakStatusChanged) {
            aBreak.setBreakEnded(breakCreation.getBreakEnded());
        }

        // Remove break from old round if location changed
        if (!oldRound.getId().equals(newRound.getId())) {
            aBreak = moveBreakFromRound(aBreak, oldRound);
        }

        // Update break round association
        aBreak.setRound(newRound);
        newRound.setBreakTime(aBreak);
        roundRepository.save(newRound);
        breakRepository.save(aBreak);

        // Calculate break start time based on previous round
        LocalDateTime breakStartTime = calculateBreakStartTime(newRound);

        // Set the break times
        aBreak.setStartTime(breakStartTime);
        aBreak.setEndTime(breakStartTime.plusMinutes(newBreakDuration));

        // Update round start time to be after the break
        newRound.setStartTime(aBreak.getEndTime());
        newRound.setEndTime(newRound.getStartTime().plusMinutes(PLAY_MINUTES));

        // Save all changes
        breakRepository.save(aBreak);
        roundRepository.save(newRound);

        // Always recalculate subsequent rounds
        updateRoundsFollowingBreak(aBreak, newRound);

        // Notify clients about the update
        webSocketService.sendMessage(ROUNDS_TOPIC, "update");
        if (breakStatusChanged) {
            sendNotificationForNextRound();
        }

        return scheduleReturnDTOService.breakToBreakDTO(newRound.getBreakTime());
    }

    private Break moveBreakFromRound(Break aBreak, Round oldRound) {
        aBreak.setRound(null);
        breakRepository.save(aBreak);
        oldRound.setBreakTime(null);
        roundRepository.save(oldRound);
        // Create new break if round changed
        Break bBreak = new Break();
        bBreak.setStartTime(aBreak.getStartTime());
        bBreak.setEndTime(aBreak.getEndTime());
        bBreak.setBreakEnded(aBreak.isBreakEnded());
        breakRepository.delete(aBreak);
        return breakRepository.save(bBreak);
    }

    private void updateRoundsFollowingBreak(Break aBreak, Round breakRound) {
        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        notPlayedRounds.sort(Comparator.comparing(Round::getRoundNumber));

        if (aBreak.isBreakEnded()) {
            updateNotPlayedRoundsSchedule(notPlayedRounds);
        } else {
            // If break is not ended but moved or duration changed, update rounds after
            // break
            List<Round> roundsAfterBreak = notPlayedRounds.stream()
                    .filter(r -> !r.getId().equals(breakRound.getId())
                            && r.getRoundNumber() > breakRound.getRoundNumber())
                    .toList();

            updateRoundsAfterBreak(roundsAfterBreak, breakRound);
        }
    }

    private void updateNotPlayedRoundsSchedule(List<Round> notPlayedRounds) {
        for (int i = 0; i < notPlayedRounds.size(); i++) {
            Round currentRound = notPlayedRounds.get(i);
            LocalDateTime startTime = LocalDateTime.now(ZoneId.systemDefault()).plusMinutes(PLAY_MINUTES * i);
            currentRound.setStartTime(startTime);
            currentRound.setEndTime(startTime.plusMinutes(PLAY_MINUTES));
            roundRepository.save(currentRound);
        }
    }

    public RoundReturnDTO updateRound(Long roundId, RoundInputFullDTO roundCreation)
            throws EntityNotFoundException, RoundsAlreadyExistsException, NotificationNotSentException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException(ROUND_NOT_FOUND));

        if (roundCreation.getGames() != null && round.getGames() != null) {
            Map<Long, Game> gamesById = round.getGames().stream()
                    .collect(Collectors.toMap(
                            Game::getId,
                            g -> g));

            for (GameInputFullDTO gameInput : roundCreation.getGames()) {
                Game game = gamesById.get(gameInput.getId());
                if (game != null) {
                    updatePointsOfGame(game, gameInput, round);
                }
            }
        }

        roundRepository.save(round);

        return updateRoundPlayed(roundId, new RoundInputDTO(roundCreation.isPlayed()));

    }

    public GameReturnDTO updateGame(Long gameId, GameInputFullDTO gameInput) throws EntityNotFoundException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));

        updatePointsOfGame(game, gameInput, game.getRound());

        Game savedGame = gameRepository.save(game);

        return scheduleReturnDTOService.gameToGameDTO(savedGame);
    }

    private void updatePointsOfGame(Game game, GameInputFullDTO gameInput, Round round) {
        // Map points by character name for efficient lookup.
        Map<String, Points> pointsByCharacter = game.getPoints().stream()
                .collect(Collectors.toMap(
                        p -> p.getTeam().getCharacter().getCharacterName(),
                        p -> p));

        for (PointsInputFullDTO pointsInput : gameInput.getPoints()) {
            Points point = pointsByCharacter.get(pointsInput.getTeam().getCharacterName());
            if (point != null) {
                // Decide whether the game belongs to the finals.
                if (round.isFinalGame()) {
                    point.setFinalPoints(pointsInput.getPoints());
                } else {
                    point.setGroupPoints(pointsInput.getPoints());
                }
                pointsRepository.save(point);
            }
        }
    }

    public void sendNotificationForNextRound() throws NotificationNotSentException {
        List<Round> unplayedRounds = roundRepository.findByPlayedFalse();
        unplayedRounds.sort(Comparator.comparing(Round::getRoundNumber));
        if (unplayedRounds.isEmpty()) {
            return;
        }
        Round round = unplayedRounds.getFirst();
        sendNotificationForNextRound(round);
    }

    public void sendNotificationForNextRound(Round round) throws NotificationNotSentException {
        List<Game> games = gameRepository.findByRoundId(round.getId());
        List<Team> teamsPlaying = new ArrayList<>();

        if (round.getBreakTime() != null && !round.getBreakTime().isBreakEnded()) {
            adminNotificationCreateService.sendNotificationToAll(
                    "It's pizza time! 🍕",
                    "Pizzapause!");
            return;
        }

        for (Game game : games) {
            List<Points> points = pointsRepository.findByGameId(game.getId());
            for (Points point : points) {
                Team team = point.getTeam();
                String title = "Du spielst jetzt an Switch " + game.getSwitchGame() + "!";
                String message = title + " Streng dich an!";

                adminNotificationCreateService.sendNotificationToTeam(
                        team.getId(),
                        title,
                        message);
                teamsPlaying.add(team);
            }
        }

        List<Team> teamsNotPlaying = teamRepository.findAll().stream()
                .filter(team -> !teamsPlaying.contains(team))
                .toList();

        for (Team team : teamsNotPlaying) {
            adminNotificationCreateService.sendNotificationToTeam(
                    team.getId(),
                    "Du spielst jetzt nicht!",
                    "Gönn dir eine Pause!");
        }
    }
}
