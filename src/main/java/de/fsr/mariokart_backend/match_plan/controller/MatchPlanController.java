package de.fsr.mariokart_backend.match_plan.controller;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.dto.*;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/match_plan")
@AllArgsConstructor
public class MatchPlanController {

    private final MatchPlanService matchPlanService;

    @GetMapping("/rounds")
    public List<RoundReturnDTO> getRounds() {
        return matchPlanService.getRounds();
    }

    @GetMapping("/rounds/current")
    public List<RoundReturnDTO> getCurrentRounds() {
        return matchPlanService.getCurrentRounds();
    }

    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<RoundReturnDTO> getRoundById(@PathVariable Long roundId) {
        try {
            return ResponseEntity.ok(matchPlanService.getRoundById(roundId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/rounds/{roundId}/games")
    public List<GameReturnDTO> getGamesByRoundId(@PathVariable Long roundId) {
        return matchPlanService.getGamesByRoundId(roundId);
    }

    @GetMapping("/games")
    public List<GameReturnDTO> getGames() {
        return matchPlanService.getGames();
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameReturnDTO> getGameById(@PathVariable Long gameId) {
        try {
            return ResponseEntity.ok(matchPlanService.getGameById(gameId));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/points")
    public List<PointsReturnDTO> getPoints() {
        return matchPlanService.getPoints();
    }

//    @GetMapping("/rounds/{id}/games/{gameId}/teams")
//    public List<Round> getTeamsByGameId(@PathVariable Long id, @PathVariable Long gameId) {
//
//        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
//
//    }
//
//    @GetMapping("/rounds/{id}/games/{gameId}/teams/{teamId}")
//    public Round getTeamByGameId(@PathVariable Long id, @PathVariable Long gameId, @PathVariable Long teamId) {
//
//        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Not implemented");
//
//    }

    @GetMapping("/create/match_plan")
    public ResponseEntity<Boolean> isMatchPlanCreated() {
        return ResponseEntity.ok(matchPlanService.isMatchPlanCreated());
    }

    @GetMapping("/create/final_plan")
    public ResponseEntity<Boolean> isFinalPlanCreated() {
        return ResponseEntity.ok(matchPlanService.isFinalPlanCreated());
    }

    @PostMapping("/create/match_plan")
    public List<RoundReturnDTO> createMatchPlan() {

        try {
            return matchPlanService.createMatchPlan();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (NotEnoughTeamsException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, e.getMessage());
        }

    }

    @PostMapping("/create/final_plan")
    public List<RoundReturnDTO> createFinalPlan() {
        try {
            return matchPlanService.createFinalPlan();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NotEnoughTeamsException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/rounds/{roundId}")
    public ResponseEntity<RoundReturnDTO> updateRoundPlayed(@PathVariable Long roundId, @RequestBody RoundInputDTO roundCreation) {
        try {
            return ResponseEntity.ok(matchPlanService.updateRoundPlayed(roundId, roundCreation));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/rounds/{roundId}/games/{gameId}/teams/{teamId}/points")
    public ResponseEntity<PointsReturnDTO> updatePoints(@PathVariable Long roundId, @PathVariable Long gameId, @PathVariable Long teamId, @RequestBody PointsInputDTO pointsCreation) {
        try {
            return ResponseEntity.ok(matchPlanService.updatePoints(roundId, gameId, teamId, pointsCreation));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }


    }

    @DeleteMapping("create/match_plan")
    public void deleteMatchPlan() {
        matchPlanService.deleteMatchPlan();
    }

    @DeleteMapping("create/final_plan")
    public void deleteFinalPlan() {
        matchPlanService.deleteFinalPlan();
    }

    @DeleteMapping("reset")
    public void reset() {
        matchPlanService.reset();
    }

}
