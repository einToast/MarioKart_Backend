package de.fsr.mariokart_backend.settings.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.schedule.service.pub.PublicScheduleReadService;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminSettingsUpdateService {

    private final TournamentRepository tournamentRepository;
    private final PublicScheduleReadService publicScheduleReadService;

    public TournamentDTO updateSettings(TournamentDTO tournamentDTO) throws RoundsAlreadyExistsException {
        Tournament tournament = tournamentRepository.findAll().get(0);
        if (tournament == null) {
            throw new IllegalStateException("Settings do not exist.");
        }
        if (tournamentDTO.getRegistrationOpen() != null && tournamentDTO.getRegistrationOpen()
                && publicScheduleReadService.isMatchPlanCreated()) {
            throw new RoundsAlreadyExistsException("Matches already exist. Can't open registration.");
        }

        if (tournamentDTO.getTournamentOpen() != null) {
            tournament.setTournamentOpen(tournamentDTO.getTournamentOpen());
        }
        if (tournamentDTO.getRegistrationOpen() != null) {
            tournament.setRegistrationOpen(tournamentDTO.getRegistrationOpen());
        }
        if (tournamentDTO.getMaxGamesCount() != null) {
            tournament.setMaxGamesCount(tournamentDTO.getMaxGamesCount());
        }

        return new TournamentDTO(tournamentRepository.save(tournament));
    }
}