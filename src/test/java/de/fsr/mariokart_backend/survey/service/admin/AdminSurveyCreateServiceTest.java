package de.fsr.mariokart_backend.survey.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSurveyCreateServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionInputDTOService questionInputDTOService;

    @Mock
    private QuestionReturnDTOService questionReturnDTOService;

    @InjectMocks
    private AdminSurveyCreateService service;

    @Test
    void createQuestionMapsSavesAndReturnsDto() {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        Question question = org.mockito.Mockito.mock(Question.class);
        QuestionReturnDTO dto = new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false);

        when(questionInputDTOService.questionInputDTOToQuestion(input)).thenReturn(question);
        when(questionRepository.save(question)).thenReturn(question);
        when(questionReturnDTOService.questionToQuestionReturnDTO(question)).thenReturn(dto);

        QuestionReturnDTO result = service.createQuestion(input);

        assertThat(result).isEqualTo(dto);
    }
}
