package de.fsr.mariokart_backend.match_plan.service;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MatchPlanDeleteService {

    private final RoundRepository roundRepository;

    public void deleteMatchPlan() {
        roundRepository.deleteAll();
    }

    public void deleteFinalPlan() {
        roundRepository.deleteAll(roundRepository.findByFinalGameTrue());
    }

}
