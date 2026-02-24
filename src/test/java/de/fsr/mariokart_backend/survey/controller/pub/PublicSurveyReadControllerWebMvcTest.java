package de.fsr.mariokart_backend.survey.controller.pub;

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
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicSurveyReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicSurveyReadControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSurveyReadService publicSurveyReadService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void getVisibleQuestionsReturnsList() throws Exception {
        when(publicSurveyReadService.getVisibleQuestions())
                .thenReturn(List.of(new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false)));

        mockMvc.perform(get("/public/survey/visible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getStatisticsReturnsValues() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L)).thenReturn(List.of(1, 2));

        mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));
    }

    @Test
    void getStatisticsMapsNotFound() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L))
                .thenThrow(new EntityNotFoundException("Question not found"));

        mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStatisticsMapsBadRequest() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L))
                .thenThrow(new IllegalArgumentException("Unsupported"));

        mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Unsupported")));
    }

    @Test
    void getStatisticsMapsConflict() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L))
                .thenThrow(new IllegalStateException("Question is not visible or is active"));

        mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Question is not visible")));
    }
}
