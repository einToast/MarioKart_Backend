package de.fsr.mariokart_backend.schedule.service.admin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Game;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
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

    private static final long PLAY_MINUTES = 20L;

    private void updateBreakAndFollowingRounds(Round breakRound, List<Round> roundsAfterBreak) {
        int breakDuration = (int) Duration
                .between(breakRound.getBreakTime().getStartTime(), breakRound.getBreakTime().getEndTime())
                .toMinutes();

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

        // Calculate the start time for the break based on previous round or current
        // time
        LocalDateTime breakStartTime;
        if (breakRoundIndex > 0) {
            // Use the end time of the previous round as break start time
            Round previousRound = allRounds.get(breakRoundIndex - 1);
            breakStartTime = previousRound.getEndTime();
        } else {
            // Fallback if there's no previous round (shouldn't happen in normal use)
            breakStartTime = LocalDateTime.now();
        }

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
            throws EntityNotFoundException, RoundsAlreadyExistsException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));

        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());

        // Only set endTime to now if the round is being marked as played
        if (roundCreation.isPlayed()) {
            round.setEndTime(LocalDateTime.now());
        }
        // End time for not played rounds will be set by updateNotPlayedRoundsSchedule

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
        } else if (!round.isPlayed() && !notPlayedRounds.contains(round)) {
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));
        }

        if (!adminScheduleReadService.isBreakFinished()
                && !notPlayedRounds.stream().anyMatch(r -> r.getBreakTime() != null)) {
            throw new RoundsAlreadyExistsException("Break not finished.");
        }

        updateNotPlayedRoundsSchedule(notPlayedRounds);

        if (!adminScheduleReadService.isBreakFinished()) {

            Round breakRound = notPlayedRounds.stream()
                    .filter(r -> r.getBreakTime() != null)
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No round with a break found."));

            List<Round> roundsAfterBreak = notPlayedRounds.stream()
                    .filter(r -> r.getStartTime().isAfter(breakRound.getStartTime()))
                    .toList();

            updateBreakAndFollowingRounds(breakRound, roundsAfterBreak);
        }

        webSocketService.sendMessage("/topic/rounds", "update");

        return scheduleReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public PointsReturnDTO updatePoints(Long roundId, Long gameId, Long teamId, PointsInputDTO pointsCreation)
            throws EntityNotFoundException {
        Points points = pointsRepository.findByGameIdAndTeamId(gameId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("There are no points with this ID."));
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
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

    public BreakReturnDTO updateBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        if (!publicScheduleReadService.isMatchPlanCreated()) {
            throw new EntityNotFoundException("Match plan not created yet.");
        }

        Break aBreak = breakRepository.findAll().get(0);
        Round oldRound = aBreak.getRound();
        Round newRound = roundRepository.findById(breakCreation.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));

        // Store old values to check if they've changed
        boolean oldBreakEnded = aBreak.isBreakEnded();
        int newBreakDuration = breakCreation.getBreakDuration();

        boolean breakStatusChanged = false;
        if (breakCreation.getBreakEnded() != null &&
                breakCreation.getBreakEnded() != oldBreakEnded) {
            aBreak.setBreakEnded(breakCreation.getBreakEnded());
            breakStatusChanged = true;
        }

        boolean locationChanged = !oldRound.getId().equals(newRound.getId());

        // Remove break from old round if location changed
        if (locationChanged) {
            oldRound.setBreakTime(null);
            roundRepository.save(oldRound);
        }

        // Update break round association
        aBreak.setRound(newRound);
        newRound.setBreakTime(aBreak);
        roundRepository.save(newRound);

        // Find the previous round to use its end time
        List<Round> allRounds = roundRepository.findAll();
        allRounds.sort(Comparator.comparing(Round::getRoundNumber));

        // Find the index of the new round with break
        int breakRoundIndex = -1;
        for (int i = 0; i < allRounds.size(); i++) {
            if (allRounds.get(i).getId().equals(newRound.getId())) {
                breakRoundIndex = i;
                break;
            }
        }

        // Calculate break start time based on previous round
        LocalDateTime breakStartTime;
        if (breakRoundIndex > 0) {
            // Use the end time of the previous round
            Round previousRound = allRounds.get(breakRoundIndex - 1);
            breakStartTime = previousRound.getEndTime();
        } else {
            // Fallback
            breakStartTime = LocalDateTime.now();
        }

        // Set the break times
        aBreak.setStartTime(breakStartTime);
        aBreak.setEndTime(breakStartTime.plusMinutes(newBreakDuration));

        // Update round start time to be after the break
        newRound.setStartTime(aBreak.getEndTime());
        newRound.setEndTime(newRound.getStartTime().plusMinutes(PLAY_MINUTES));

        // Save all changes
        breakRepository.save(aBreak);
        roundRepository.save(newRound);

        boolean breakDurationChanged = true; // Always recalculate subsequent rounds

        // Update rounds after break if break has ended, duration changed, or break
        // moved
        if (breakStatusChanged || breakDurationChanged || locationChanged) {
            List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

            if (aBreak.isBreakEnded()) {
                // If break ended, update all not played rounds
                updateNotPlayedRoundsSchedule(notPlayedRounds);
            } else {
                // If break is not ended but moved or duration changed, update rounds after
                // break
                List<Round> roundsAfterBreak = notPlayedRounds.stream()
                        .filter(r -> !r.getId().equals(newRound.getId())
                                && r.getStartTime().isAfter(aBreak.getStartTime()))
                        .toList();

                updateRoundsAfterBreak(roundsAfterBreak, newRound);
            }

            // Notify clients about the update
            webSocketService.sendMessage("/topic/rounds", "update");
        }

        return scheduleReturnDTOService.breakToBreakDTO(newRound.getBreakTime());
    }

    private void updateNotPlayedRoundsSchedule(List<Round> notPlayedRounds) {
        for (int i = 0; i < notPlayedRounds.size(); i++) {
            Round currentRound = notPlayedRounds.get(i);
            LocalDateTime startTime = LocalDateTime.now().plusMinutes(PLAY_MINUTES * i);
            currentRound.setStartTime(startTime);
            currentRound.setEndTime(startTime.plusMinutes(PLAY_MINUTES));
            roundRepository.save(currentRound);
        }
    }
}