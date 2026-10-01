package de.fsr.mariokart_backend.survey.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.SurveyKeyRequiredException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.settings.model.SurveyKeyMode;
import de.fsr.mariokart_backend.settings.model.Tournament;
import de.fsr.mariokart_backend.settings.repository.TournamentRepository;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.SurveyKey;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerSubmissionResult;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.repository.SurveyKeyRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicSurveyCreateServiceTest {

    private static final String USER_JSON = "{\"teamId\":7}";

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerInputDTOService answerInputDTOService;

    @Mock
    private AnswerReturnDTOService answerReturnDTOService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private SurveyKeyRepository surveyKeyRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PublicSurveyCreateService service;

    @Test
    void submitAnswerThrowsWhenUserJsonMissing() {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "text", null, null, null);

        assertThatThrownBy(() -> service.submitAnswer(input, "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User JSON is null or empty");
    }

    @Test
    void submitAnswerThrowsWhenQuestionNotActiveOrVisible() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "text", null, null, null);
        Question question = new MultipleChoiceQuestion();
        question.setId(1L);
        question.setActive(false);
        question.setVisible(true);

        when(objectMapper.readValue(eq(USER_JSON), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.submitAnswer(input, USER_JSON, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active or visible");
    }

    @Test
    void submitAnswerThrowsWhenTeamReachedMaxAnswers() throws Exception {
        stubTeamAndQuestion();
        AnswerInputDTO input = new AnswerInputDTO(1L, "CHECKBOX", null, null, List.of(1), null);

        when(answerRepository.countByQuestionIdAndSubmittingTeamId(1L, 7L)).thenReturn(4L);

        assertThatThrownBy(() -> service.submitAnswer(input, USER_JSON, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum number of answers per team reached");
    }

    @Test
    void submitAnswerThrowsForDuplicateTeamOneFreeTextAnswer() throws Exception {
        stubTeamAndQuestion();
        AnswerInputDTO input = new AnswerInputDTO(1L, "TEAM_ONE_FREE_TEXT", "abc", null, null, 0);

        when(answerRepository.existsByQuestionIdAndSubmittingTeamId(1L, 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.submitAnswer(input, USER_JSON, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already submitted an answer");
    }

    @Test
    void submitAnswerReturnsMappedDtoForValidInput() throws Exception {
        stubTeamAndQuestion();
        AnswerInputDTO input = freeTextInput();
        FreeTextAnswer mapped = new FreeTextAnswer();
        mapped.setTextAnswer("lets go");

        AnswerReturnDTO expected = new AnswerReturnDTO(1L, "FREE_TEXT", "lets go", null, null, null);

        when(tournamentRepository.findAll()).thenReturn(List.of());
        when(answerInputDTOService.answerInputDTOToAnswer(input, 7L)).thenReturn(mapped);
        when(answerRepository.save(mapped)).thenReturn(mapped);
        when(answerReturnDTOService.answerToAnswerReturnDTO(mapped)).thenReturn(expected);

        AnswerSubmissionResult result = service.submitAnswer(input, USER_JSON, null);

        assertThat(result.answer()).isEqualTo(expected);
        assertThat(result.issuedSurveyKey()).isNull();
    }

    @Test
    void submitAnswerIgnoresSurveyKeyWhenKeysDisabled() throws Exception {
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.DISABLED, true);

        AnswerSubmissionResult result = service.submitAnswer(freeTextInput(), USER_JSON, "some-key");

        assertThat(result.issuedSurveyKey()).isNull();
        assertThat(mapped.getSurveyKey()).isNull();
        verify(surveyKeyRepository, never()).findByToken(any());
    }

    @Test
    void submitAnswerIssuesSurveyKeyWhileDistributing() throws Exception {
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.DISTRIBUTING, false);
        when(surveyKeyRepository.save(any(SurveyKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerSubmissionResult result = service.submitAnswer(freeTextInput(), USER_JSON, null);

        assertThat(result.issuedSurveyKey()).isNotBlank();
        assertThat(mapped.getSurveyKey().getToken()).isEqualTo(result.issuedSurveyKey());
    }

    @Test
    void submitAnswerIssuesNewSurveyKeyWhenCookieKeyUnknownWhileDistributing() throws Exception {
        stubValidFreeTextSubmission(SurveyKeyMode.DISTRIBUTING, false);
        when(surveyKeyRepository.findByToken("stale-key")).thenReturn(Optional.empty());
        when(surveyKeyRepository.save(any(SurveyKey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AnswerSubmissionResult result = service.submitAnswer(freeTextInput(), USER_JSON, "stale-key");

        assertThat(result.issuedSurveyKey()).isNotBlank().isNotEqualTo("stale-key");
    }

    @Test
    void submitAnswerReusesExistingSurveyKeyWhileDistributing() throws Exception {
        SurveyKey key = new SurveyKey(5L, "known-key");
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.DISTRIBUTING, false);
        when(surveyKeyRepository.findByToken("known-key")).thenReturn(Optional.of(key));

        AnswerSubmissionResult result = service.submitAnswer(freeTextInput(), USER_JSON, "known-key");

        assertThat(result.issuedSurveyKey()).isNull();
        assertThat(mapped.getSurveyKey()).isSameAs(key);
        verify(surveyKeyRepository, never()).save(any());
    }

    @Test
    void submitAnswerAcceptsValidSurveyKeyWhenRequired() throws Exception {
        SurveyKey key = new SurveyKey(5L, "known-key");
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.REQUIRED, false);
        when(surveyKeyRepository.findByToken("known-key")).thenReturn(Optional.of(key));

        AnswerSubmissionResult result = service.submitAnswer(freeTextInput(), USER_JSON, "known-key");

        assertThat(result.issuedSurveyKey()).isNull();
        assertThat(mapped.getSurveyKey()).isSameAs(key);
    }

    @Test
    void submitAnswerThrowsWhenSurveyKeyMissingAndRequired() throws Exception {
        stubTeamAndQuestion();
        when(tournamentRepository.findAll()).thenReturn(List.of(tournament(SurveyKeyMode.REQUIRED)));

        assertThatThrownBy(() -> service.submitAnswer(freeTextInput(), USER_JSON, null))
                .isInstanceOf(SurveyKeyRequiredException.class)
                .hasMessageContaining("valid survey key is required");
        verify(answerRepository, never()).save(any());
    }

    @Test
    void submitAnswerThrowsWhenSurveyKeyUnknownAndRequired() throws Exception {
        stubTeamAndQuestion();
        when(tournamentRepository.findAll()).thenReturn(List.of(tournament(SurveyKeyMode.REQUIRED)));
        when(surveyKeyRepository.findByToken("forged-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.submitAnswer(freeTextInput(), USER_JSON, "forged-key"))
                .isInstanceOf(SurveyKeyRequiredException.class);
        verify(surveyKeyRepository, never()).save(any());
    }

    @Test
    void submitAnswerThrowsWhenSurveyKeyAlreadyAnsweredAndQuestionAllowsOneAnswerPerKey() throws Exception {
        stubTeamAndQuestion(true);
        when(tournamentRepository.findAll()).thenReturn(List.of(tournament(SurveyKeyMode.REQUIRED)));
        when(surveyKeyRepository.findByToken("known-key")).thenReturn(Optional.of(new SurveyKey(5L, "known-key")));
        when(answerRepository.existsByQuestionIdAndSurveyKeyId(1L, 5L)).thenReturn(true);

        AnswerInputDTO input = freeTextInput();

        assertThatThrownBy(() -> service.submitAnswer(input, USER_JSON, "known-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("survey key has already been used");
        verify(answerRepository, never()).save(any());
    }

    @Test
    void submitAnswerAllowsFirstAnswerOfSurveyKeyWhenQuestionAllowsOneAnswerPerKey() throws Exception {
        SurveyKey key = new SurveyKey(5L, "known-key");
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.REQUIRED, true);
        when(surveyKeyRepository.findByToken("known-key")).thenReturn(Optional.of(key));
        when(answerRepository.existsByQuestionIdAndSurveyKeyId(1L, 5L)).thenReturn(false);

        service.submitAnswer(freeTextInput(), USER_JSON, "known-key");

        assertThat(mapped.getSurveyKey()).isSameAs(key);
    }

    @Test
    void submitAnswerAllowsRepeatedAnswersOfSurveyKeyWhenQuestionDoesNotLimitPerKey() throws Exception {
        SurveyKey key = new SurveyKey(5L, "known-key");
        FreeTextAnswer mapped = stubValidFreeTextSubmission(SurveyKeyMode.REQUIRED, false);
        when(surveyKeyRepository.findByToken("known-key")).thenReturn(Optional.of(key));

        service.submitAnswer(freeTextInput(), USER_JSON, "known-key");

        assertThat(mapped.getSurveyKey()).isSameAs(key);
        verify(answerRepository, never()).existsByQuestionIdAndSurveyKeyId(any(), any());
    }

    private AnswerInputDTO freeTextInput() {
        return new AnswerInputDTO(1L, "FREE_TEXT", "lets go", null, null, null);
    }

    private Tournament tournament(SurveyKeyMode mode) {
        return new Tournament(1L, true, false, 4, mode);
    }

    private void stubTeamAndQuestion() throws Exception {
        stubTeamAndQuestion(false);
    }

    private void stubTeamAndQuestion(boolean oneAnswerPerKey) throws Exception {
        Team team = new Team();
        team.setId(7L);
        Question question = new MultipleChoiceQuestion();
        question.setId(1L);
        question.setActive(true);
        question.setVisible(true);
        question.setOneAnswerPerKey(oneAnswerPerKey);

        when(objectMapper.readValue(eq(USER_JSON), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
    }

    private FreeTextAnswer stubValidFreeTextSubmission(SurveyKeyMode mode, boolean oneAnswerPerKey)
            throws Exception {
        stubTeamAndQuestion(oneAnswerPerKey);
        FreeTextAnswer mapped = new FreeTextAnswer();
        mapped.setTextAnswer("lets go");

        when(tournamentRepository.findAll()).thenReturn(List.of(tournament(mode)));
        when(answerInputDTOService.answerInputDTOToAnswer(any(AnswerInputDTO.class), eq(7L))).thenReturn(mapped);
        when(answerRepository.save(mapped)).thenReturn(mapped);
        when(answerReturnDTOService.answerToAnswerReturnDTO(mapped))
                .thenReturn(new AnswerReturnDTO(1L, "FREE_TEXT", "lets go", null, null, null));
        return mapped;
    }
}
