package de.fsr.mariokart_backend.survey.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;

@Tag("unit")
class AnswerReturnDTOServiceTest {

    private final AnswerReturnDTOService service = new AnswerReturnDTOService();

    @Test
    void mapsMultipleChoiceAnswer() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(1L);

        MultipleChoiceAnswer answer = new MultipleChoiceAnswer();
        answer.setQuestion(question);
        answer.setSelectedOption(2);

        AnswerReturnDTO dto = service.answerToAnswerReturnDTO(answer);

        assertThat(dto.getQuestionId()).isEqualTo(1L);
        assertThat(dto.getAnswerType()).isEqualTo("MULTIPLE_CHOICE");
        assertThat(dto.getMultipleChoiceSelectedOption()).isEqualTo(2);
    }

    @Test
    void mapsTeamAnswerUsingIndexFromQuestionTeams() {
        Team alpha = team(1L, "Alpha");
        Team beta = team(2L, "Beta");
        TeamQuestion question = new TeamQuestion();
        question.setId(9L);
        question.setTeams(List.of(alpha, beta));

        TeamAnswer answer = new TeamAnswer();
        answer.setQuestion(question);
        answer.setTeam(beta);

        AnswerReturnDTO dto = service.answerToAnswerReturnDTO(answer);

        assertThat(dto.getAnswerType()).isEqualTo("TEAM");
        assertThat(dto.getTeamSelectedOption()).isEqualTo(1);
    }

    @Test
    void throwsWhenTeamAnswerQuestionTypeIsInvalid() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(10L);
        TeamAnswer answer = new TeamAnswer();
        answer.setQuestion(question);
        answer.setTeam(team(1L, "Alpha"));

        assertThatThrownBy(() -> service.answerToAnswerReturnDTO(answer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid question type");
    }

    @Test
    void mapsTeamOneFreeTextAnswer() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(11L);
        Team team = team(7L, "Alpha");

        TeamOneFreeTextAnswer answer = new TeamOneFreeTextAnswer();
        answer.setQuestion(question);
        answer.setTeam(team);
        answer.setTextAnswer("Great");

        AnswerReturnDTO dto = service.answerToAnswerReturnDTO(answer);

        assertThat(dto.getAnswerType()).isEqualTo("TEAM_ONE_FREE_TEXT");
        assertThat(dto.getFreeTextAnswer()).isEqualTo("Great");
        assertThat(dto.getTeamSelectedOption()).isEqualTo(7);
    }

    private Team team(Long id, String name) {
        Team team = new Team();
        team.setId(id);
        team.setTeamName(name);
        return team;
    }
}
