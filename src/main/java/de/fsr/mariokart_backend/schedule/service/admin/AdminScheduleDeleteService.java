package de.fsr.mariokart_backend.schedule.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminScheduleDeleteService {

    private final RoundRepository roundRepository;

    public void deleteMatchPlan() {
        roundRepository.deleteAll();
    }

    public void deleteFinalPlan() {
        roundRepository.deleteAllByFinalGameTrue();
    }
}