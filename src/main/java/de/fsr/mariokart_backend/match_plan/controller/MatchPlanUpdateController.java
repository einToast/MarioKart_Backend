package de.fsr.mariokart_backend.match_plan.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.BreakReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.PointsReturnDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundInputDTO;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanUpdateService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/match_plan")
@AllArgsConstructor
public class MatchPlanUpdateController {

    private final MatchPlanUpdateService matchPlanUpdateService;

    @PutMapping("/rounds/{roundId}")
    public ResponseEntity<RoundReturnDTO> updateRoundPlayed(@PathVariable Long roundId,
            @RequestBody RoundInputDTO roundCreation) {
        try {
            return ResponseEntity.ok(matchPlanUpdateService.updateRoundPlayed(roundId, roundCreation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RoundsAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/rounds/{roundId}/games/{gameId}/teams/{teamId}/points")
    public ResponseEntity<PointsReturnDTO> updatePoints(@PathVariable Long roundId, @PathVariable Long gameId,
            @PathVariable Long teamId, @RequestBody PointsInputDTO pointsCreation) {
        try {
            return ResponseEntity.ok(matchPlanUpdateService.updatePoints(roundId, gameId, teamId, pointsCreation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/break")
    public ResponseEntity<BreakReturnDTO> updateBreak(@RequestBody BreakInputDTO breakCreation) {
        try {
            return ResponseEntity.ok(matchPlanUpdateService.updateBreak(breakCreation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}