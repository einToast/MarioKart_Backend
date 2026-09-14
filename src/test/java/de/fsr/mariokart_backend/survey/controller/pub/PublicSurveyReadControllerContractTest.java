package de.fsr.mariokart_backend.survey.controller.pub;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(PublicSurveyReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicSurveyReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/survey/public-survey-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSurveyReadService publicSurveyReadService;

    @Test
    void getVisibleQuestionsMatchesContract() throws Exception {
        when(publicSurveyReadService.getVisibleQuestions())
                .thenReturn(List.of(new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false)));

        MvcResult result = mockMvc.perform(get("/public/survey/visible"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_visible_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsMatchesContract() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L)).thenReturn(List.of(1, 2));

        MvcResult result = mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_id_statistics_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsNotFoundMatchesContract() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L)).thenThrow(new EntityNotFoundException("Question not found"));

        MvcResult result = mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_statistics_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsBadRequestMatchesContract() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L)).thenThrow(new IllegalArgumentException("Unsupported"));

        MvcResult result = mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_statistics_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsConflictMatchesContract() throws Exception {
        when(publicSurveyReadService.getStatisticsOfQuestion(2L)).thenThrow(new IllegalStateException("Question not visible"));

        MvcResult result = mockMvc.perform(get("/public/survey/2/statistics"))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_statistics_error_409",
                result.getResponse().getContentAsString());
    }
}
