package de.fsr.mariokart_backend.match_plan.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.match_plan.service.MatchPlanDeleteService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/match_plan")
@AllArgsConstructor
public class MatchPlanDeleteController {

    private final MatchPlanDeleteService matchPlanDeleteService;

    @DeleteMapping("/create/match_plan")
    public void deleteMatchPlan() {
        matchPlanDeleteService.deleteMatchPlan();
    }

    @DeleteMapping("/create/final_plan")
    public void deleteFinalPlan() {
        matchPlanDeleteService.deleteFinalPlan();
    }
}