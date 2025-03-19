package de.fsr.mariokart_backend.settings.service.pub;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicSettingsReadService {

    private final TournamentRepository tournamentRepository;

    public TournamentDTO getSettings() {
        if (tournamentRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Settings do not exist.");
        }
        return new TournamentDTO(tournamentRepository.findAll().get(0));
    }
}