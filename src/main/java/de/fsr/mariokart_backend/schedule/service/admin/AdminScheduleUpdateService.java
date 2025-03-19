package de.fsr.mariokart_backend.schedule.service.admin;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.model.Break;
import de.fsr.mariokart_backend.schedule.model.Points;
import de.fsr.mariokart_backend.schedule.model.Round;
import de.fsr.mariokart_backend.schedule.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.schedule.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.PointsRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.schedule.service.dto.ScheduleReturnDTOService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminScheduleUpdateService {

    private final RoundRepository roundRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final WebSocketService webSocketService;
    private final ScheduleReturnDTOService scheduleReturnDTOService;

    private static final long PLAY_MINUTES = 20L;

    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation)
            throws EntityNotFoundException, RoundsAlreadyExistsException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));

        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());
        if (roundCreation.isPlayed()) {
            round.setEndTime(LocalDateTime.now());
        }

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
            updateNotPlayedRoundsSchedule(notPlayedRounds);
        } else if (!round.isPlayed() && !notPlayedRounds.contains(round)) {
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));
            updateNotPlayedRoundsSchedule(notPlayedRounds);
        }

        Round savedRound = roundRepository.save(round);
        webSocketService.sendMessage("/topic/rounds", "update");

        return scheduleReturnDTOService.roundToRoundDTO(savedRound);
    }

    public PointsReturnDTO updatePoints(Long roundId, Long gameId, Long teamId, PointsInputDTO pointsCreation)
            throws EntityNotFoundException {
        Points points = pointsRepository.findByGameIdAndTeamId(gameId, teamId)
                .orElseThrow(() -> new EntityNotFoundException("There are no points with this ID."));
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));

        if (round.isFinalGame()) {
            points.setFinalPoints(pointsCreation.getPoints());
        } else {
            points.setGroupPoints(pointsCreation.getPoints());
        }

        return scheduleReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
    }

    public BreakReturnDTO updateBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        Break aBreak = breakRepository.findAll().get(0);
        aBreak.setBreakEnded(breakCreation.getBreakEnded());

        if (breakCreation.getBreakEnded()) {
            List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
            notPlayedRounds.sort((r1, r2) -> r1.getStartTime().compareTo(r2.getStartTime()));

            LocalDateTime currentTime = LocalDateTime.now();
            for (int i = 0; i < notPlayedRounds.size(); i++) {
                Round round = notPlayedRounds.get(i);
                round.setStartTime(currentTime.plusMinutes(PLAY_MINUTES * i));
                round.setEndTime(round.getStartTime().plusMinutes(PLAY_MINUTES));
                roundRepository.save(round);
            }
        }

        breakRepository.save(aBreak);
        webSocketService.sendMessage("/topic/rounds", "update");

        return scheduleReturnDTOService.breakToBreakDTO(aBreak);
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