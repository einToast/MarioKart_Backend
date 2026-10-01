package de.fsr.mariokart_backend.survey.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSurveyUpdateServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionInputDTOService questionInputDTOService;

    @Mock
    private QuestionReturnDTOService questionReturnDTOService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private AdminNotificationCreateService adminNotificationCreateService;

    @InjectMocks
    private AdminSurveyUpdateService service;

    @Test
    void updateQuestionThrowsWhenQuestionMissing() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateQuestion(1L, new QuestionInputDTO()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("question with this id");
    }

    @Test
    void updateQuestionThrowsForUnsupportedQuestionType() {
        Question existing = new FreeTextQuestion();
        Question unsupported = new Question() {
        };
        unsupported.setActive(false);
        unsupported.setVisible(false);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(questionInputDTOService.questionInputDTOToQuestion(any(QuestionInputDTO.class))).thenReturn(unsupported);

        QuestionInputDTO input = new QuestionInputDTO();

        assertThatThrownBy(() -> service.updateQuestion(1L, input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question type not supported");
    }

    @Test
    void updateQuestionUpdatesMultipleChoiceAndSendsWebSocketUpdate() throws Exception {
        MultipleChoiceQuestion existing = new MultipleChoiceQuestion();
        existing.setId(1L);
        existing.setQuestionText("Old");
        existing.setActive(true);
        existing.setVisible(true);
        existing.setLive(false);
        existing.setOptions(List.of("A", "B"));

        MultipleChoiceQuestion updated = new MultipleChoiceQuestion();
        updated.setQuestionText("New");
        updated.setActive(true);
        updated.setVisible(true);
        updated.setLive(true);
        updated.setOneAnswerPerKey(true);
        updated.setOptions(List.of("X", "Y"));

        QuestionReturnDTO dto = new QuestionReturnDTO(1L, "MULTIPLE_CHOICE", "New", List.of("X", "Y"), true, true, true, false, false);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(questionInputDTOService.questionInputDTOToQuestion(any(QuestionInputDTO.class))).thenReturn(updated);
        when(questionRepository.save(existing)).thenReturn(existing);
        when(questionReturnDTOService.questionToQuestionReturnDTO(existing)).thenReturn(dto);

        QuestionReturnDTO result = service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(result).isEqualTo(dto);
        assertThat(existing.getQuestionText()).isEqualTo("New");
        assertThat(existing.getOptions()).containsExactly("X", "Y");
        assertThat(existing.getLive()).isTrue();
        assertThat(existing.isOneAnswerPerKey()).isTrue();
        verify(webSocketService).sendMessage("/topic/questions", "update");
        verifyNoInteractions(adminNotificationCreateService);
    }

    @Test
    void updateQuestionNotifiesWhenQuestionBecomesAnswerable() throws Exception {
        FreeTextQuestion existing = new FreeTextQuestion();
        existing.setId(3L);
        existing.setQuestionText("Old");
        existing.setActive(false);
        existing.setVisible(false);

        FreeTextQuestion updated = new FreeTextQuestion();
        updated.setQuestionText("New Poll");
        updated.setActive(true);
        updated.setVisible(true);

        QuestionReturnDTO dto = new QuestionReturnDTO(3L, "FREE_TEXT", "New Poll", List.of(), true, true, false, false, false);

        when(questionRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(questionInputDTOService.questionInputDTOToQuestion(any(QuestionInputDTO.class))).thenReturn(updated);
        when(questionRepository.save(existing)).thenReturn(existing);
        when(questionReturnDTOService.questionToQuestionReturnDTO(existing)).thenReturn(dto);

        service.updateQuestion(3L, new QuestionInputDTO());

        verify(webSocketService).sendMessage("/topic/questions", "update");
        verify(adminNotificationCreateService).sendNotificationToAll("Neue Umfrage verfügbar!", "New Poll");
    }

    @Test
    void updateQuestionUpdatesCheckboxOptions() throws Exception {
        CheckboxQuestion existing = new CheckboxQuestion();
        existing.setActive(false);
        existing.setVisible(false);
        existing.setOptions(List.of("A", "B"));

        CheckboxQuestion updated = new CheckboxQuestion();
        updated.setActive(false);
        updated.setVisible(false);
        updated.setOptions(List.of("X", "Y", "Z"));

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getOptions()).containsExactly("X", "Y", "Z");
        verifyNoInteractions(adminNotificationCreateService);
    }

    @Test
    void updateQuestionKeepsCheckboxOptionsWhenNoneAreSent() throws Exception {
        CheckboxQuestion existing = new CheckboxQuestion();
        existing.setActive(false);
        existing.setVisible(false);
        existing.setOptions(List.of("A", "B"));

        CheckboxQuestion updated = new CheckboxQuestion();
        updated.setActive(false);
        updated.setVisible(false);

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getOptions()).containsExactly("A", "B");
    }

    @Test
    void updateQuestionUpdatesTeamQuestionFields() throws Exception {
        Team oldTeam = new Team();
        oldTeam.setId(1L);
        Team newTeam = new Team();
        newTeam.setId(2L);

        TeamQuestion existing = new TeamQuestion();
        existing.setActive(false);
        existing.setVisible(false);
        existing.setFinalTeamsOnly(false);
        existing.setTeams(List.of(oldTeam));

        TeamQuestion updated = new TeamQuestion();
        updated.setActive(false);
        updated.setVisible(false);
        updated.setFinalTeamsOnly(true);
        updated.setTeams(List.of(newTeam));

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getFinalTeamsOnly()).isTrue();
        assertThat(existing.getTeams()).containsExactly(newTeam);
    }

    @Test
    void updateQuestionOnlyUpdatesCommonFieldsOfTeamOneFreeTextQuestion() throws Exception {
        TeamOneFreeTextQuestion existing = new TeamOneFreeTextQuestion();
        existing.setQuestionText("Old");
        existing.setActive(false);
        existing.setVisible(false);

        TeamOneFreeTextQuestion updated = new TeamOneFreeTextQuestion();
        updated.setQuestionText("New");
        updated.setActive(false);
        updated.setVisible(true);

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getQuestionText()).isEqualTo("New");
        assertThat(existing.getVisible()).isTrue();
        verify(webSocketService).sendMessage("/topic/questions", "update");
        verifyNoInteractions(adminNotificationCreateService);
    }

    @Test
    void updateQuestionDoesNotNotifyWhenQuestionWasAlreadyActive() throws Exception {
        FreeTextQuestion existing = new FreeTextQuestion();
        existing.setActive(true);
        existing.setVisible(false);

        FreeTextQuestion updated = new FreeTextQuestion();
        updated.setActive(true);
        updated.setVisible(true);

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getVisible()).isTrue();
        verifyNoInteractions(adminNotificationCreateService);
    }

    @Test
    void updateQuestionKeepsFlagsAndDoesNotNotifyWhenFlagsAreMissing() throws Exception {
        FreeTextQuestion existing = new FreeTextQuestion();
        existing.setQuestionText("Old");
        existing.setActive(false);
        existing.setVisible(true);

        FreeTextQuestion updated = new FreeTextQuestion();

        stubUpdate(existing, updated);

        service.updateQuestion(1L, new QuestionInputDTO());

        assertThat(existing.getQuestionText()).isEqualTo("Old");
        assertThat(existing.getActive()).isFalse();
        assertThat(existing.getVisible()).isTrue();
        verifyNoInteractions(adminNotificationCreateService);
    }

    private void stubUpdate(Question existing, Question updated) {
        when(questionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(questionInputDTOService.questionInputDTOToQuestion(any(QuestionInputDTO.class))).thenReturn(updated);
        when(questionRepository.save(existing)).thenReturn(existing);
    }
}
