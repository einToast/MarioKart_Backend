package de.fsr.mariokart_backend.schedule.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = {"schedule", "registration"})
@CacheEvict(allEntries = true)
@Transactional(rollbackFor = Exception.class)
public class AdminScheduleDeleteService {

    private final RoundRepository roundRepository;

    public void deleteSchedule() {
        roundRepository.deleteAll();
    }

    public void deleteFinalSchedule() {
        roundRepository.deleteAllByFinalGameTrue();
    }
}