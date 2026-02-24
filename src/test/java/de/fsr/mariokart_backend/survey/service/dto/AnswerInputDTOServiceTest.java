package de.fsr.mariokart_backend.survey.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AnswerInputDTOServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private AnswerInputDTOService service;

    @Test
    void mapsMultipleChoiceAnswer() throws EntityNotFoundException {
        Team submitting = team(50L, "Submitter");
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(1L);

        AnswerInputDTO input = new AnswerInputDTO(1L, "MULTIPLE_CHOICE", null, 2, null, null);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(50L)).thenReturn(Optional.of(submitting));

        Answer answer = service.answerInputDTOToAnswer(input, 50L);

        assertThat(answer).isInstanceOf(MultipleChoiceAnswer.class);
        assertThat(((MultipleChoiceAnswer) answer).getSelectedOption()).isEqualTo(2);
        assertThat(answer.getQuestion()).isSameAs(question);
        assertThat(answer.getSubmittingTeam()).isSameAs(submitting);
    }

    @Test
    void mapsTeamAnswerUsingQuestionOptionIndex() throws EntityNotFoundException {
        Team selectedTeam = team(1L, "Alpha");
        Team submitting = team(2L, "Bravo");
        TeamQuestion teamQuestion = new TeamQuestion();
        teamQuestion.setId(10L);
        teamQuestion.setTeams(List.of(selectedTeam));

        AnswerInputDTO input = new AnswerInputDTO(10L, "TEAM", null, null, null, 0);
        when(questionRepository.findById(10L)).thenReturn(Optional.of(teamQuestion), Optional.of(teamQuestion));
        when(teamRepository.findByTeamName("Alpha")).thenReturn(Optional.of(selectedTeam));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(submitting));

        Answer answer = service.answerInputDTOToAnswer(input, 2L);

        assertThat(answer).isInstanceOf(TeamAnswer.class);
        assertThat(((TeamAnswer) answer).getTeam()).isSameAs(selectedTeam);
        assertThat(answer.getSubmittingTeam()).isSameAs(submitting);
    }

    @Test
    void throwsForInvalidAnswerType() {
        AnswerInputDTO input = new AnswerInputDTO(1L, "INVALID", null, null, null, null);

        assertThatThrownBy(() -> service.answerInputDTOToAnswer(input, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid answer type");
    }

    @Test
    void throwsWhenTeamAnswerHasNonTeamQuestion() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(10L);
        AnswerInputDTO input = new AnswerInputDTO(10L, "TEAM", null, null, null, 0);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.answerInputDTOToAnswer(input, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid question type");
    }

    @Test
    void mapsTeamOneFreeTextAnswer() throws EntityNotFoundException {
        Team selectedTeam = team(3L, "Chosen");
        Team submitting = team(4L, "Submitter");
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(77L);

        AnswerInputDTO input = new AnswerInputDTO(77L, "TEAM_ONE_FREE_TEXT", "hello", null, null, 3);

        when(teamRepository.findById(3L)).thenReturn(Optional.of(selectedTeam));
        when(questionRepository.findById(77L)).thenReturn(Optional.of(question));
        when(teamRepository.findById(4L)).thenReturn(Optional.of(submitting));

        Answer answer = service.answerInputDTOToAnswer(input, 4L);

        assertThat(answer).isInstanceOf(TeamOneFreeTextAnswer.class);
        TeamOneFreeTextAnswer mapped = (TeamOneFreeTextAnswer) answer;
        assertThat(mapped.getTextAnswer()).isEqualTo("hello");
        assertThat(mapped.getTeam()).isSameAs(selectedTeam);
        assertThat(answer.getSubmittingTeam()).isSameAs(submitting);
    }

    private Team team(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        return team;
    }
}
