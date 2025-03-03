package de.fsr.mariokart_backend.match_plan.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.GameReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanReadService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/match_plan")
@AllArgsConstructor
public class MatchPlanReadController {

    private final MatchPlanReadService matchPlanReadService;

    @GetMapping("/rounds")
    public List<RoundReturnDTO> getRounds() {
        return matchPlanReadService.getRounds();
    }

    @GetMapping("/rounds/current")
    public List<RoundReturnDTO> getCurrentRounds() {
        return matchPlanReadService.getCurrentRounds();
    }

    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<RoundReturnDTO> getRoundById(@PathVariable Long roundId) {
        try {
            return ResponseEntity.ok(matchPlanReadService.getRoundById(roundId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/rounds/{roundId}/games")
    public List<GameReturnDTO> getGamesByRoundId(@PathVariable Long roundId) {
        return matchPlanReadService.getGamesByRoundId(roundId);
    }

    @GetMapping("/games")
    public List<GameReturnDTO> getGames() {
        return matchPlanReadService.getGames();
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameReturnDTO> getGameById(@PathVariable Long gameId) {
        try {
            return ResponseEntity.ok(matchPlanReadService.getGameById(gameId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/points")
    public List<PointsReturnDTO> getPoints() {
        return matchPlanReadService.getPoints();
    }

    @GetMapping("/break")
    public BreakReturnDTO getBreak() {
        return matchPlanReadService.getBreak();
    }
}