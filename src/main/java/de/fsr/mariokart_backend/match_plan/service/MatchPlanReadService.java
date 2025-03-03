package de.fsr.mariokart_backend.match_plan.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.match_plan.model.Round;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.repository.BreakRepository;
import de.fsr.mariokart_backend.match_plan.repository.GameRepository;
import de.fsr.mariokart_backend.match_plan.repository.PointsRepository;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.match_plan.service.dto.MatchPlanReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanReadService {

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final PointsRepository pointsRepository;
    private final BreakRepository breakRepository;
    private final MatchPlanReturnDTOService matchPlanReturnDTOService;

    public List<RoundReturnDTO> getRounds() {
        return roundRepository.findAll().stream()
                .sorted(Comparator.comparing(Round::getRoundNumber))
                .map(matchPlanReturnDTOService::roundToRoundDTO)
                .toList();
    }

    public RoundReturnDTO getRoundById(Long roundId) throws EntityNotFoundException {
        return roundRepository.findById(roundId)
                .map(matchPlanReturnDTOService::roundToRoundDTO)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this ID."));
    }

    public RoundReturnDTO getRoundByRoundNumber(int roundNumber) throws EntityNotFoundException {
        return roundRepository.findByRoundNumber(roundNumber)
                .map(matchPlanReturnDTOService::roundToRoundDTO)
                .orElseThrow(() -> new EntityNotFoundException("There is no round with this round number."));
    }

    public List<RoundReturnDTO> getCurrentRounds() {
        List<Round> rounds = roundRepository.findByPlayedFalse();

        rounds.sort(Comparator.comparing(Round::getStartTime));
        if (rounds.size() > 2) {
            rounds = rounds.subList(0, 2);
        }
        return rounds.stream()
                .map(matchPlanReturnDTOService::roundToRoundDTO)
                .toList();
    }

    public List<GameReturnDTO> getGamesByRoundId(Long gameId) {
        return gameRepository.findByRoundId(gameId).stream()
                .map(matchPlanReturnDTOService::gameToGameDTO)
                .toList();
    }

    public List<GameReturnDTO> getGames() {
        return gameRepository.findAll().stream()
                .map(matchPlanReturnDTOService::gameToGameDTO)
                .toList();
    }

    public GameReturnDTO getGameById(Long gameId) throws EntityNotFoundException {
        return matchPlanReturnDTOService.gameToGameDTO(gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("There is no game with this ID.")));
    }

    public List<PointsReturnDTO> getPoints() {
        return pointsRepository.findAll().stream()
                .map(matchPlanReturnDTOService::pointsToPointsDTO)
                .toList();
    }

    public BreakReturnDTO getBreak() {
        return matchPlanReturnDTOService.breakToBreakDTO(breakRepository.findAll().get(0));
    }

    public Boolean isBreakFinished() {
        return breakRepository.findAll().get(0).isBreakEnded();
    }

    public Boolean isMatchPlanCreated() {
        return !roundRepository.findAll().isEmpty();
    }

    public Boolean isFinalPlanCreated() {
        return !roundRepository.findByFinalGameTrue().isEmpty();
    }

}