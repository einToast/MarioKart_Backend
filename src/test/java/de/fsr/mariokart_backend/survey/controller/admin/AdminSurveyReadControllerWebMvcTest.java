package de.fsr.mariokart_backend.survey.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminSurveyReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminSurveyReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSurveyReadService adminSurveyReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getQuestionsReturnsList() throws Exception {
        when(adminSurveyReadService.getQuestions())
                .thenReturn(List.of(new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false)));

        mockMvc.perform(get("/admin/survey"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAnswersOfQuestionReturnsList() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenReturn(List.of(new AnswerReturnDTO(2L, "FREE_TEXT", "A", null, null, null)));

        mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(2));
    }

    @Test
    void getAnswersOfQuestionMapsNotFound() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenThrow(new EntityNotFoundException("There is no question with this id."));

        mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("question")));
    }

    @Test
    void getAnswersOfQuestionMapsBadRequest() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenThrow(new IllegalArgumentException("Unsupported question type"));

        mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unsupported")));
    }

    @Test
    void getStatisticsOfQuestionReturnsValues() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L)).thenReturn(List.of(1, 2, 3));

        mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
    }

    @Test
    void getStatisticsOfQuestionMapsNotFound() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L))
                .thenThrow(new EntityNotFoundException("Question not found"));

        mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatisticsOfQuestionMapsBadRequest() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L))
                .thenThrow(new IllegalArgumentException("Unsupported"));

        mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNumberOfAnswersReturnsCount() throws Exception {
        when(adminSurveyReadService.getNumberOfAnswers(4L)).thenReturn(7);

        mockMvc.perform(get("/admin/survey/4/answers/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }
}
