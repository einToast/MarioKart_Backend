package de.fsr.mariokart_backend.survey.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.registration.repository.TeamRepository;
import de.fsr.mariokart_backend.registration.service.admin.AdminRegistrationReadService;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class QuestionInputDTOServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private AdminRegistrationReadService adminRegistrationReadService;

    @InjectMocks
    private QuestionInputDTOService service;

    @Test
    void mapsMultipleChoiceQuestion() {
        QuestionInputDTO input = new QuestionInputDTO(
                "Favorite track?",
                "MULTIPLE_CHOICE",
                List.of("A", "B"),
                true,
                true,
                false,
                false);

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question).isInstanceOf(MultipleChoiceQuestion.class);
        assertThat(((MultipleChoiceQuestion) question).getOptions()).containsExactly("A", "B");
        assertThat(question.getQuestionText()).isEqualTo("Favorite track?");
        assertThat(question.getActive()).isTrue();
        assertThat(question.getVisible()).isTrue();
        assertThat(question.getLive()).isFalse();
    }

    @Test
    void mapsTeamQuestionUsingFinalTeamsWhenRequested() {
        Team alpha = team(1L, "Alpha");
        QuestionInputDTO input = new QuestionInputDTO(
                "Who wins?",
                "TEAM",
                null,
                true,
                true,
                false,
                true);
        when(adminRegistrationReadService.getFinalTeams()).thenReturn(List.of(alpha));

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question).isInstanceOf(TeamQuestion.class);
        TeamQuestion teamQuestion = (TeamQuestion) question;
        assertThat(teamQuestion.getFinalTeamsOnly()).isTrue();
        assertThat(teamQuestion.getTeams()).containsExactly(alpha);
    }

    @Test
    void mapsTeamQuestionUsingAllTeamsWhenFinalOnlyDisabled() {
        Team alpha = team(1L, "Alpha");
        Team beta = team(2L, "Beta");
        QuestionInputDTO input = new QuestionInputDTO(
                "Who wins?",
                "TEAM",
                null,
                true,
                true,
                false,
                false);
        when(teamRepository.findAll()).thenReturn(List.of(alpha, beta));

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question).isInstanceOf(TeamQuestion.class);
        TeamQuestion teamQuestion = (TeamQuestion) question;
        assertThat(teamQuestion.getFinalTeamsOnly()).isFalse();
        assertThat(teamQuestion.getTeams()).containsExactly(alpha, beta);
    }

    @Test
    void throwsForInvalidQuestionType() {
        QuestionInputDTO input = new QuestionInputDTO(
                "bad",
                "INVALID_TYPE",
                null,
                true,
                true,
                true,
                false);

        assertThatThrownBy(() -> service.questionInputDTOToQuestion(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid question type");
    }

    private Team team(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        return team;
    }
}
