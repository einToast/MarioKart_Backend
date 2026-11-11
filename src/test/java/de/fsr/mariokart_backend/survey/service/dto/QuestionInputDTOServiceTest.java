package de.fsr.mariokart_backend.survey.service.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

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
import de.fsr.mariokart_backend.survey.model.QuestionTranslation;
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
                false,
                false, null);

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question).isInstanceOf(MultipleChoiceQuestion.class);
        assertThat(((MultipleChoiceQuestion) question).getOptions()).containsExactly("A", "B");
        assertThat(question.getQuestionText()).isEqualTo("Favorite track?");
        assertThat(question.getActive()).isTrue();
        assertThat(question.getVisible()).isTrue();
        assertThat(question.getLive()).isFalse();
        assertThat(question.isOneAnswerPerKey()).isFalse();
    }

    @Test
    void mapsOneAnswerPerKeyFlag() {
        QuestionInputDTO input = new QuestionInputDTO(
                "Best kart?",
                "FREE_TEXT",
                null,
                true,
                true,
                false,
                false,
                true, null);

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question.isOneAnswerPerKey()).isTrue();
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
                true,
                false, null);
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
                false,
                false, null);
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
                false,
                false, null);

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

    @Test
    void mapsTrimmedTranslationsFittedToTheOptions() {
        QuestionInputDTO input = new QuestionInputDTO("Wer gewinnt?", "MULTIPLE_CHOICE", List.of("Rot", "Blau"),
                false, false, false, false, false, Map.of(
                        "en", new QuestionTranslation(" Who wins? ", List.of(" ", "Blue", "Green")),
                        "fr", new QuestionTranslation(" ", List.of("", " "))));

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question.getTranslations())
                .containsOnlyKeys("en")
                .containsEntry("en", new QuestionTranslation("Who wins?", List.of("", "Blue")));
    }

    @Test
    void dropsOptionTranslationsOfQuestionsWithoutOptions() {
        QuestionInputDTO input = new QuestionInputDTO("Feedback?", "FREE_TEXT", List.of(), false, false, false,
                false, false, Map.of("en", new QuestionTranslation("Feedback?", List.of("A"))));

        Question question = service.questionInputDTOToQuestion(input);

        assertThat(question.getTranslations()).containsEntry("en", new QuestionTranslation("Feedback?", null));
    }

    @Test
    void leavesTranslationsUnsetWhenNoneAreSent() {
        QuestionInputDTO input = new QuestionInputDTO("Q?", "FREE_TEXT", null, false, false, false, false, false, null);

        assertThat(service.questionInputDTOToQuestion(input).getTranslations()).isNull();
    }

    @Test
    void rejectsInvalidTranslations() {
        QuestionInputDTO invalidLanguage = new QuestionInputDTO("Q?", "FREE_TEXT", null, false, false, false, false,
                false, Map.of("English", new QuestionTranslation("Q?", null)));
        QuestionInputDTO tooLong = new QuestionInputDTO("Q?", "FREE_TEXT", null, false, false, false, false,
                false, Map.of("en", new QuestionTranslation("x".repeat(256), null)));

        assertThatThrownBy(() -> service.questionInputDTOToQuestion(invalidLanguage))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.questionInputDTOToQuestion(tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
