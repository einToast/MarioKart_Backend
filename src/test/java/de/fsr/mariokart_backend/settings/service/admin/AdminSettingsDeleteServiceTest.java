package de.fsr.mariokart_backend.settings.service.admin;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationDeleteService;
import de.fsr.mariokart_backend.schedule.repository.BreakRepository;
import de.fsr.mariokart_backend.schedule.repository.RoundRepository;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSettingsDeleteServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private BreakRepository breakRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AdminRegistrationDeleteService adminRegistrationDeleteService;

    @InjectMocks
    private AdminSettingsDeleteService service;

    @Test
    void resetDeletesFlushesThenRecreatesTournament() throws Exception {
        service.reset();

        InOrder order = inOrder(roundRepository, questionRepository, answerRepository, breakRepository,
                tournamentRepository, adminRegistrationDeleteService);

        order.verify(roundRepository).deleteAll();
        order.verify(questionRepository).deleteAll();
        order.verify(answerRepository).deleteAll();
        order.verify(breakRepository).deleteAll();
        order.verify(tournamentRepository).deleteAll();

        order.verify(roundRepository).flush();
        order.verify(questionRepository).flush();
        order.verify(answerRepository).flush();
        order.verify(breakRepository).flush();
        order.verify(tournamentRepository).flush();

        order.verify(adminRegistrationDeleteService).deleteAllTeams();
        order.verify(tournamentRepository).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resetInvokesRegistrationCleanup() throws Exception {
        service.reset();

        verify(adminRegistrationDeleteService).deleteAllTeams();
    }
}
