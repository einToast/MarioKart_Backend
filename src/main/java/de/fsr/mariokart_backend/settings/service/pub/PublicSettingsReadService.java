package de.fsr.mariokart_backend.settings.service.pub;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "settings")
public class PublicSettingsReadService {

    private final TournamentRepository tournamentRepository;

    @Cacheable(key = "'tournamentSettings'", sync = true)
    public TournamentDTO getSettings() {
        if (tournamentRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Settings do not exist.");
        }
        return new TournamentDTO(tournamentRepository.findAll().getFirst());
    }
}