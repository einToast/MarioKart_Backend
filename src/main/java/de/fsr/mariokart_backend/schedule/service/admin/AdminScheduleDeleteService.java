package de.fsr.mariokart_backend.schedule.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "schedule")
@CacheEvict(allEntries = true)
public class AdminScheduleDeleteService {

    private final RoundRepository roundRepository;

    public void deleteSchedule() {
        roundRepository.deleteAll();
    }

    public void deleteFinalSchedule() {
        roundRepository.deleteAllByFinalGameTrue();
    }
}