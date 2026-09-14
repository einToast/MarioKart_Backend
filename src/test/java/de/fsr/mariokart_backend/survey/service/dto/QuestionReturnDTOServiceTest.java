package de.fsr.mariokart_backend.survey.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.registration.model.Team;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;

@Tag("unit")
class QuestionReturnDTOServiceTest {

    private final QuestionReturnDTOService service = new QuestionReturnDTOService();

    @Test
    void mapsFreeTextQuestion() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(1L);
        question.setQuestionText("Feedback?");
        question.setActive(true);
        question.setVisible(true);
        question.setLive(false);

        QuestionReturnDTO dto = service.questionToQuestionReturnDTO(question);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getQuestionType()).isEqualTo("FREE_TEXT");
        assertThat(dto.getQuestionText()).isEqualTo("Feedback?");
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void mapsTeamQuestionOptionsAndFlag() {
        TeamQuestion question = new TeamQuestion();
        question.setId(2L);
        question.setQuestionText("Winner?");
        question.setActive(true);
        question.setVisible(true);
        question.setLive(true);
        question.setFinalTeamsOnly(true);
        question.setTeams(List.of(team(1L, "Alpha"), team(2L, "Beta")));

        QuestionReturnDTO dto = service.questionToQuestionReturnDTO(question);

        assertThat(dto.getQuestionType()).isEqualTo("TEAM");
        assertThat(dto.getOptions()).containsExactly("Alpha", "Beta");
        assertThat(dto.isFinalTeamsOnly()).isTrue();
    }

    @Test
    void throwsForUnsupportedQuestionType() {
        Question unsupported = new Question() {
        };
        unsupported.setId(99L);
        unsupported.setQuestionText("x");
        unsupported.setActive(true);
        unsupported.setVisible(true);
        unsupported.setLive(true);

        assertThatThrownBy(() -> service.questionToQuestionReturnDTO(unsupported))
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
