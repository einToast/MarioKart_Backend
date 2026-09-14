package de.fsr.mariokart_backend.survey.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import de.fsr.mariokart_backend.survey.model.Answer;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSurveyReadServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionReturnDTOService questionReturnDTOService;

    @Mock
    private AnswerReturnDTOService answerReturnDTOService;

    @InjectMocks
    private AdminSurveyReadService service;

    @Test
    void getAnswersOfQuestionThrowsWhenQuestionMissing() {
        when(questionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAnswersOfQuestion(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void getAnswersOfQuestionThrowsWhenQuestionTypeIsNotFreeText() {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion();
        question.setId(1L);
        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.getAnswersOfQuestion(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a FreeTextQuestion");
    }

    @Test
    void getAnswersOfQuestionReturnsMappedAnswersForFreeTextQuestion() throws EntityNotFoundException {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(1L);

        FreeTextAnswer answer = new FreeTextAnswer();
        answer.setQuestion(question);
        answer.setTextAnswer("Hello");
        AnswerReturnDTO dto = new AnswerReturnDTO(1L, "FREE_TEXT", "Hello", null, null, null);

        when(questionRepository.findById(1L)).thenReturn(Optional.of(question));
        when(answerRepository.findAllByQuestionId(1L)).thenReturn(List.of(answer));
        when(answerReturnDTOService.answerToAnswerReturnDTO(answer)).thenReturn(dto);

        List<AnswerReturnDTO> result = service.getAnswersOfQuestion(1L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getStatisticsOfQuestionCountsMultipleChoiceSelections() throws EntityNotFoundException {
        MultipleChoiceQuestion question = new MultipleChoiceQuestion();
        question.setId(11L);
        question.setOptions(List.of("A", "B", "C"));

        MultipleChoiceAnswer first = new MultipleChoiceAnswer();
        first.setSelectedOption(0);
        MultipleChoiceAnswer second = new MultipleChoiceAnswer();
        second.setSelectedOption(0);
        MultipleChoiceAnswer third = new MultipleChoiceAnswer();
        third.setSelectedOption(2);
        MultipleChoiceAnswer invalid = new MultipleChoiceAnswer();
        invalid.setSelectedOption(8);

        when(questionRepository.findById(11L)).thenReturn(Optional.of(question));
        when(answerRepository.findAllByQuestionId(11L))
                .thenReturn(List.of(first, second, third, invalid));

        assertThat(service.getStatisticsOfQuestion(11L)).containsExactly(2, 0, 1);
    }

    @Test
    void getStatisticsOfQuestionCountsCheckboxSelections() throws EntityNotFoundException {
        CheckboxQuestion question = new CheckboxQuestion();
        question.setId(12L);
        question.setOptions(List.of("A", "B", "C"));

        CheckboxAnswer first = new CheckboxAnswer();
        first.setSelectedOptions(List.of(0, 2));
        CheckboxAnswer second = new CheckboxAnswer();
        second.setSelectedOptions(List.of(2, 1));
        CheckboxAnswer invalid = new CheckboxAnswer();
        invalid.setSelectedOptions(List.of(9));

        when(questionRepository.findById(12L)).thenReturn(Optional.of(question));
        when(answerRepository.findAllByQuestionId(12L)).thenReturn(List.of(first, second, invalid));

        assertThat(service.getStatisticsOfQuestion(12L)).containsExactly(1, 1, 2);
    }

    @Test
    void getStatisticsOfQuestionCountsTeamSelections() throws EntityNotFoundException {
        Team alpha = team(1L, "Alpha");
        Team beta = team(2L, "Beta");
        TeamQuestion question = new TeamQuestion();
        question.setId(13L);
        question.setTeams(List.of(alpha, beta));

        TeamAnswer first = new TeamAnswer();
        first.setTeam(alpha);
        TeamAnswer second = new TeamAnswer();
        second.setTeam(beta);
        TeamAnswer third = new TeamAnswer();
        third.setTeam(beta);
        TeamAnswer invalid = new TeamAnswer();
        invalid.setTeam(team(99L, "Ghost"));

        when(questionRepository.findById(13L)).thenReturn(Optional.of(question));
        when(answerRepository.findAllByQuestionId(13L)).thenReturn(List.of(first, second, third, invalid));

        assertThat(service.getStatisticsOfQuestion(13L)).containsExactly(1, 2);
    }

    @Test
    void getStatisticsOfQuestionThrowsForUnsupportedType() {
        Question unsupported = new FreeTextQuestion();
        unsupported.setId(99L);

        when(questionRepository.findById(99L)).thenReturn(Optional.of(unsupported));
        when(answerRepository.findAllByQuestionId(99L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.getStatisticsOfQuestion(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not supported");
    }

    @Test
    void getNumberOfAnswersReturnsRepositoryCount() {
        when(answerRepository.findAllByQuestionId(5L)).thenReturn(List.of(new FreeTextAnswer(), new FreeTextAnswer()));

        assertThat(service.getNumberOfAnswers(5L)).isEqualTo(2);
    }

    @Test
    void getQuestionsMapsRepositoryQuestions() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(21L);
        when(questionRepository.findAll()).thenReturn(List.of(question));
        when(questionReturnDTOService.questionToQuestionReturnDTO(any(Question.class)))
                .thenReturn(new de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO());

        assertThat(service.getQuestions()).hasSize(1);
    }

    private Team team(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        return team;
    }
}
