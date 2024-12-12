package de.fsr.mariokart_backend.settings.service;

import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SettingsService {

    private final TournamentRepository tournamentRepository;

    public TournamentDTO getSettings() {
        if (tournamentRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Settings do not exist.");
        }
        return new TournamentDTO(tournamentRepository.findAll().get(0));
    }

    public TournamentDTO createSettings() {
        if (!tournamentRepository.findAll().isEmpty()) {
            throw new IllegalStateException("Settings already exist.");
        }
        return new TournamentDTO(tournamentRepository.save(new Tournament()));
    }

    public TournamentDTO updateSettings(TournamentDTO tournamentDTO) {
        Tournament tournament = tournamentRepository.findAll().get(0);
        if (tournament == null) {
            throw new IllegalStateException("Settings do not exist.");
        }
        if (tournamentDTO.getTournamentOpen() != null){
            tournament.setTournamentOpen(tournamentDTO.getTournamentOpen());
        }
        if (tournamentDTO.getRegistrationOpen() != null){
            tournament.setRegistrationOpen(tournamentDTO.getRegistrationOpen());
        }

        if (tournamentDTO.getMaxGamesCount() != null){
            tournament.setMaxGamesCount(tournamentDTO.getMaxGamesCount());
        }

        return new TournamentDTO(tournamentRepository.save(tournament));
    }

}
