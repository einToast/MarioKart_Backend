package de.fsr.mariokart_backend.settings.service.admin;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.RoundsAlreadyExistsException;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationDeleteService;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminSettingsDeleteService {

    private final TournamentRepository tournamentRepository;
    private final BreakRepository breakRepository;
    private final RoundRepository roundRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final AdminRegistrationDeleteService adminRegistrationDeleteService;

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

        adminRegistrationDeleteService.deleteAllTeams();

        tournamentRepository.save(new Tournament());
    }
}