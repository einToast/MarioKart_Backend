package de.fsr.mariokart_backend.settings.service;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.match_plan.repository.BreakRepository;
import de.fsr.mariokart_backend.match_plan.repository.RoundRepository;
import de.fsr.mariokart_backend.registration.service.DeleteRegistrationService;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.model.dto.TournamentDTO;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SettingsService {

    private final TournamentRepository tournamentRepository;
    private final BreakRepository breakRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    private final DeleteRegistrationService deleteRegistrationService;

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

    public TournamentDTO updateSettings(TournamentDTO tournamentDTO) throws RoundsAlreadyExistsException {
        Tournament tournament = tournamentRepository.findAll().get(0);
        if (tournament == null) {
            throw new IllegalStateException("Settings do not exist.");
        }
        if (tournamentDTO.getRegistrationOpen() != null && tournamentDTO.getRegistrationOpen()
                && !roundRepository.findAll().isEmpty()) {
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

    public void reset() throws RoundsAlreadyExistsException {
        roundRepository.deleteAll();
        questionRepository.deleteAll();
        answerRepository.deleteAll();
        breakRepository.deleteAll();
        tournamentRepository.deleteAll();
        roundRepository.flush();
        questionRepository.flush();
        answerRepository.flush();
        breakRepository.flush();
        tournamentRepository.flush();

        deleteRegistrationService.deleteAllTeams();

        tournamentRepository.save(new Tournament());
    }

}
