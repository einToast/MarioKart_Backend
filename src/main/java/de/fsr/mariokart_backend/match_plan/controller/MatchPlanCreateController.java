package de.fsr.mariokart_backend.match_plan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotEnoughTeamsException;
import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.model.dto.RoundReturnDTO;
import de.fsr.mariokart_backend.match_plan.service.MatchPlanCreateService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/match_plan")
@AllArgsConstructor
public class MatchPlanCreateController {

    private final MatchPlanCreateService matchPlanCreateService;

    @PostMapping("/create/match_plan")
    public List<RoundReturnDTO> createMatchPlan() {
        try {
            return matchPlanCreateService.createMatchPlan();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (NotEnoughTeamsException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/create/final_plan")
    public List<RoundReturnDTO> createFinalPlan() {
        try {
            return matchPlanCreateService.createFinalPlan();
        } catch (RoundsAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (NotEnoughTeamsException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}