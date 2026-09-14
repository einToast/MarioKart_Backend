package de.fsr.mariokart_backend.survey.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicSurveyCreateServiceTest {

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
    private ObjectMapper objectMapper;

    @InjectMocks
    private PublicSurveyCreateService service;

    @Test
    void submitAnswerThrowsWhenUserJsonMissing() {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "text", null, null, null);

        assertThatThrownBy(() -> service.submitAnswer(input, ""))
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

        when(objectMapper.readValue(eq("{\"teamId\":7}"), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.submitAnswer(input, "{\"teamId\":7}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not active or visible");
    }

    @Test
    void submitAnswerThrowsWhenTeamReachedMaxAnswers() throws Exception {
        Team team = new Team();
        team.setId(7L);
        Question question = new MultipleChoiceQuestion();
        question.setId(1L);
        question.setActive(true);
        question.setVisible(true);

        AnswerInputDTO input = new AnswerInputDTO(1L, "CHECKBOX", null, null, List.of(1), null);
        List<Answer> existing = List.of(
                buildAnswer(question, team),
                buildAnswer(question, team),
                buildAnswer(question, team),
                buildAnswer(question, team));

        when(objectMapper.readValue(eq("{\"teamId\":7}"), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(answerRepository.findAll()).thenReturn(existing);

        assertThatThrownBy(() -> service.submitAnswer(input, "{\"teamId\":7}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum number of answers per team reached");
    }

    @Test
    void submitAnswerThrowsForDuplicateTeamOneFreeTextAnswer() throws Exception {
        Team team = new Team();
        team.setId(7L);
        Question question = new MultipleChoiceQuestion();
        question.setId(1L);
        question.setActive(true);
        question.setVisible(true);

        AnswerInputDTO input = new AnswerInputDTO(1L, "TEAM_ONE_FREE_TEXT", "abc", null, null, 0);
        List<Answer> existing = List.of(buildAnswer(question, team));

        when(objectMapper.readValue(eq("{\"teamId\":7}"), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(answerRepository.findAll()).thenReturn(existing);

        assertThatThrownBy(() -> service.submitAnswer(input, "{\"teamId\":7}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already submitted an answer");
    }

    @Test
    void submitAnswerReturnsMappedDtoForValidInput() throws Exception {
        Team team = new Team();
        team.setId(7L);
        Question question = new MultipleChoiceQuestion();
        question.setId(1L);
        question.setActive(true);
        question.setVisible(true);

        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "lets go", null, null, null);
        FreeTextAnswer mapped = new FreeTextAnswer();
        mapped.setQuestion(question);
        mapped.setSubmittingTeam(team);
        mapped.setTextAnswer("lets go");

        AnswerReturnDTO expected = new AnswerReturnDTO(1L, "FREE_TEXT", "lets go", null, null, null);

        when(objectMapper.readValue(eq("{\"teamId\":7}"), any(TypeReference.class))).thenReturn(Map.of("teamId", 7));
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(answerInputDTOService.answerInputDTOToAnswer(input, 7L)).thenReturn(mapped);
        when(answerRepository.save(mapped)).thenReturn(mapped);
        when(answerReturnDTOService.answerToAnswerReturnDTO(mapped)).thenReturn(expected);

        AnswerReturnDTO result = service.submitAnswer(input, "{\"teamId\":7}");

        assertThat(result).isEqualTo(expected);
    }

    private Answer buildAnswer(Question question, Team team) {
        FreeTextAnswer answer = new FreeTextAnswer();
        answer.setQuestion(question);
        answer.setSubmittingTeam(team);
        answer.setTextAnswer("x");
        return answer;
    }
}
