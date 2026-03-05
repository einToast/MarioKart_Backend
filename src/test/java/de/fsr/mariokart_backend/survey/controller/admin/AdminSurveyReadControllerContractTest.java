package de.fsr.mariokart_backend.survey.controller.admin;

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
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyReadService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminSurveyReadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminSurveyReadControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/survey/admin-survey-read-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSurveyReadService adminSurveyReadService;

    @Test
    void getQuestionsMatchesContract() throws Exception {
        when(adminSurveyReadService.getQuestions())
                .thenReturn(List.of(new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false)));

        MvcResult result = mockMvc.perform(get("/admin/survey"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_root_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getAnswersOfQuestionMatchesContract() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenReturn(List.of(new AnswerReturnDTO(2L, "FREE_TEXT", "A", null, null, null)));

        MvcResult result = mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_id_answers_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getAnswersOfQuestionNotFoundMatchesContract() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenThrow(new EntityNotFoundException("No question"));

        MvcResult result = mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_answers_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void getAnswersOfQuestionBadRequestMatchesContract() throws Exception {
        when(adminSurveyReadService.getAnswersOfQuestion(2L))
                .thenThrow(new IllegalArgumentException("Unsupported"));

        MvcResult result = mockMvc.perform(get("/admin/survey/2/answers"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_answers_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsOfQuestionMatchesContract() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L)).thenReturn(List.of(1, 2, 3));

        MvcResult result = mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_id_statistics_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsOfQuestionNotFoundMatchesContract() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L))
                .thenThrow(new EntityNotFoundException("No question"));

        MvcResult result = mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_statistics_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void getStatisticsOfQuestionBadRequestMatchesContract() throws Exception {
        when(adminSurveyReadService.getStatisticsOfQuestion(3L))
                .thenThrow(new IllegalArgumentException("Unsupported"));

        MvcResult result = mockMvc.perform(get("/admin/survey/3/statistics"))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "get_id_statistics_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void getNumberOfAnswersMatchesContract() throws Exception {
        when(adminSurveyReadService.getNumberOfAnswers(4L)).thenReturn(7);

        MvcResult result = mockMvc.perform(get("/admin/survey/4/answers/count"))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "get_id_answers_count_success",
                result.getResponse().getContentAsString());
    }
}
