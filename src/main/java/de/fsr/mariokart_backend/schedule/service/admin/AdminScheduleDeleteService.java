package de.fsr.mariokart_backend.schedule.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = {"schedule", "registration"})
@CacheEvict(allEntries = true)
@Transactional(rollbackFor = Exception.class)
public class AdminScheduleDeleteService {

    private static final String ROUNDS_TOPIC = "/topic/rounds";

    private final RoundRepository roundRepository;
    private final WebSocketService webSocketService;

    public void deleteSchedule() {
        roundRepository.deleteAll();
        webSocketService.sendMessage(ROUNDS_TOPIC, "delete");
    }

    public void deleteFinalSchedule() {
        roundRepository.deleteAllByFinalGameTrue();
        webSocketService.sendMessage(ROUNDS_TOPIC, "delete");
    }
}
