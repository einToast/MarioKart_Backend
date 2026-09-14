package de.fsr.mariokart_backend.survey.service.pub;

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
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyReadService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicSurveyReadServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionReturnDTOService questionReturnDTOService;

    @Mock
    private AdminSurveyReadService adminSurveyReadService;

    @InjectMocks
    private PublicSurveyReadService service;

    @Test
    void getVisibleQuestionsMapsVisibleQuestions() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(1L);
        question.setVisible(true);

        QuestionReturnDTO dto = new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false);

        when(questionRepository.findAllByVisible(true)).thenReturn(List.of(question));
        when(questionReturnDTOService.questionToQuestionReturnDTO(question)).thenReturn(dto);

        List<QuestionReturnDTO> result = service.getVisibleQuestions();

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getStatisticsOfQuestionThrowsWhenQuestionMissing() {
        when(questionRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatisticsOfQuestion(2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Question not found");
    }

    @Test
    void getStatisticsOfQuestionThrowsWhenQuestionNotEligible() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(3L);
        question.setVisible(false);
        question.setActive(false);

        when(questionRepository.findById(3L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.getStatisticsOfQuestion(3L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not visible or is active");
    }

    @Test
    void getStatisticsOfQuestionDelegatesWhenQuestionEligible() throws Exception {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setId(4L);
        question.setVisible(true);
        question.setActive(false);

        when(questionRepository.findById(4L)).thenReturn(Optional.of(question));
        when(adminSurveyReadService.getStatisticsOfQuestion(4L)).thenReturn(List.of(2, 1));

        List<Integer> result = service.getStatisticsOfQuestion(4L);

        assertThat(result).containsExactly(2, 1);
    }
}
