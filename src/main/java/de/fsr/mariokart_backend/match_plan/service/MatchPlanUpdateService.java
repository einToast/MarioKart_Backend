package de.fsr.mariokart_backend.match_plan.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
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

    public RoundReturnDTO updateRoundPlayed(Long roundId, RoundInputDTO roundCreation) throws EntityNotFoundException {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
        long playMinutes = 20L;
        List<Round> rounds = roundRepository.findByStartTimeAfter(round.getStartTime());
        List<Round> notPlayedRounds = roundRepository.findByPlayedFalse();
        rounds.sort(Comparator.comparing(Round::getStartTime));
        notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));

        round.setPlayed(roundCreation.isPlayed());

        round.setEndTime(LocalDateTime.now());

        // if (round.isPlayed() && !rounds.isEmpty()) {
        // for (int i = 0; i < rounds.size(); i++) {
        // rounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(playMinutes * i));
        // rounds.get(i).setEndTime(LocalDateTime.now().plusMinutes(playMinutes *
        // i).plusMinutes(playMinutes));
        // roundRepository.save(rounds.get(i));
        // }
        // }

        if (round.isPlayed() && !notPlayedRounds.isEmpty()) {
            notPlayedRounds.remove(round);
        }

        if (!round.isPlayed() && !notPlayedRounds.contains(round)) {
            notPlayedRounds.add(round);
            notPlayedRounds.sort(Comparator.comparing(Round::getStartTime));
        }

        for (int i = 0; i < notPlayedRounds.size(); i++) {
            notPlayedRounds.get(i).setStartTime(LocalDateTime.now().plusMinutes(playMinutes * i));
            notPlayedRounds.get(i)
                    .setEndTime(LocalDateTime.now().plusMinutes(playMinutes * i).plusMinutes(playMinutes));
            roundRepository.save(notPlayedRounds.get(i));
        }

        Round breakRound = notPlayedRounds.stream()
                .filter(r -> r.getBreakTime() != null)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No round with a break found."));

        List<Round> roundsAfterBreak = notPlayedRounds.stream()
                .filter(r -> r.getStartTime().isAfter(breakRound.getStartTime()))
                .toList();

        int breakDuration = (int) Duration
                .between(breakRound.getBreakTime().getStartTime(), breakRound.getBreakTime().getEndTime()).toMinutes();

        System.out.println(breakDuration);
        System.out.println(breakRound.getStartTime());

        breakRound.getBreakTime().setStartTime(breakRound.getStartTime());
        breakRound.getBreakTime().setEndTime(breakRound.getStartTime().plusMinutes(breakDuration));

        breakRound.setStartTime(breakRound.getBreakTime().getEndTime());
        breakRound.setEndTime(breakRound.getStartTime().plusMinutes(playMinutes));

        System.out.println(breakRound.getStartTime());

        roundRepository.save(breakRound);
        breakRepository.save(breakRound.getBreakTime());

        for (int i = 0; i < roundsAfterBreak.size(); i++) {
            roundsAfterBreak.get(i).setStartTime(breakRound.getEndTime().plusMinutes(playMinutes * i));
            roundsAfterBreak.get(i)
                    .setEndTime(breakRound.getEndTime().plusMinutes(playMinutes * i).plusMinutes(playMinutes));
            roundRepository.save(roundsAfterBreak.get(i));
        }

        // TODO: enum for possible send/receive topics
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
