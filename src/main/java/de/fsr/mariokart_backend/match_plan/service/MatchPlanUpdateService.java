package de.fsr.mariokart_backend.match_plan.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.Break;
import de.fsr.mariokart_backend.match_plan.model.Game;
import de.fsr.mariokart_backend.match_plan.model.Points;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.repository.BreakRepository;
import de.fsr.mariokart_backend.match_plan.repository.GameRepository;
import de.fsr.mariokart_backend.match_plan.repository.PointsRepository;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanReturnDTOService;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanUpdateService {

    private final RoundRepository roundRepository;
    private final WebSocketService webSocketService;
    private final MatchPlanReturnDTOService matchPlanReturnDTOService;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final TeamRepository teamRepository;
    private final MatchPlanReadService matchPlanReadService;

    private static final long PLAY_MINUTES = 20L;

    private void updateNotPlayedRoundsSchedule(List<Round> notPlayedRounds) {
        for (int i = 0; i < notPlayedRounds.size(); i++) {
            Round currentRound = notPlayedRounds.get(i);
            LocalDateTime startTime = LocalDateTime.now().plusMinutes(PLAY_MINUTES * i);
            currentRound.setStartTime(startTime);
            currentRound.setEndTime(startTime.plusMinutes(PLAY_MINUTES));
            roundRepository.save(currentRound);
        }
    }

    private void updateBreakAndFollowingRounds(Round breakRound, List<Round> roundsAfterBreak) {
        int breakDuration = (int) Duration
                .between(breakRound.getBreakTime().getStartTime(), breakRound.getBreakTime().getEndTime())
                .toMinutes();

        breakRound.getBreakTime().setStartTime(breakRound.getStartTime());
        breakRound.getBreakTime().setEndTime(breakRound.getStartTime().plusMinutes(breakDuration));

        breakRound.setStartTime(breakRound.getBreakTime().getEndTime());
        breakRound.setEndTime(breakRound.getStartTime().plusMinutes(PLAY_MINUTES));

        roundRepository.save(breakRound);
        breakRepository.save(breakRound.getBreakTime());

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

    // TODO:If played is set to false, the times of all rounds must be updated correctly
    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation)
            throws EntityNotFoundException, RoundsAlreadyExistsException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));

        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());
        round.setEndTime(LocalDateTime.now());

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
        } else if (!round.isPlayed() && !notPlayedRounds.contains(round)) {
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));
        }

        if (!matchPlanReadService.isBreakFinished()
                && !notPlayedRounds.stream().anyMatch(r -> r.getBreakTime() != null)) {
            throw new RoundsAlreadyExistsException("Break not finished.");
        }

        updateNotPlayedRoundsSchedule(notPlayedRounds);

        if (!matchPlanReadService.isBreakFinished()) {

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

        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public RoundReturnDTO updateRound(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        round.setPlayed(roundCreation.isPlayed());
        return matchPlanReturnDTOService.roundToRoundDTO(roundRepository.save(round));
    }

    public GameReturnDTO updateGame(Long gameId, GameInputDTO gameCreation) throws EntityNotFoundException {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID."));
        game.setSwitchGame(gameCreation.getSwitchGame());
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.save(game));
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
        return matchPlanReturnDTOService.pointsToPointsDTO(pointsRepository.save(points));
    }

    // TODO: check if this is correct, if break ended, the round starttime after break must be update to date of round before oder Datetime.now()
    public BreakReturnDTO updateBreak(BreakInputDTO breakCreation) throws EntityNotFoundException {
        if (!matchPlanReadService.isMatchPlanCreated()) {
            throw new EntityNotFoundException("Match plan not created yet.");
        }

        Break aBreak = breakRepository.findAll().get(0);
        Round oldRound = aBreak.getRound();
        Round newRound = roundRepository.findById(breakCreation.getRoundId())
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        aBreak.setRound(newRound);
        aBreak.setStartTime(newRound.getStartTime().minusMinutes(breakCreation.getBreakDuration()));
        aBreak.setEndTime(newRound.getStartTime());
        if (breakCreation.getBreakEnded() != null) {
            aBreak.setBreakEnded(breakCreation.getBreakEnded());
        }
        oldRound.setBreakTime(null);
        roundRepository.save(oldRound);
        Break newBreak = breakRepository.save(aBreak);
        newRound.setBreakTime(newBreak);
        roundRepository.save(newRound);
        return matchPlanReturnDTOService.breakToBreakDTO(newRound.getBreakTime());
    }

}
