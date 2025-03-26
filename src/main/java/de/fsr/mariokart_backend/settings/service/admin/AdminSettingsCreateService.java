package de.fsr.mariokart_backend.settings.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminSettingsCreateService {

    private final TournamentRepository tournamentRepository;

    public TournamentDTO createSettings() {
        if (!tournamentRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Settings already exist.");
        }
        return new TournamentDTO(tournamentRepository.save(new Tournament()));
    }
}