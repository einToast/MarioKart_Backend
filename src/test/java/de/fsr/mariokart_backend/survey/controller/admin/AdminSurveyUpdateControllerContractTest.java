package de.fsr.mariokart_backend.survey.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminSurveyUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminSurveyUpdateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/survey/admin-survey-update-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSurveyUpdateService adminSurveyUpdateService;

    @Test
    void updateQuestionSuccessMatchesContract() throws Exception {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        QuestionReturnDTO response = new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false);
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class))).thenReturn(response);

        MvcResult result = mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "put_id_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateQuestionNotFoundMatchesContract() throws Exception {
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new EntityNotFoundException("No question"));

        MvcResult result = mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_id_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateQuestionBadRequestMatchesContract() throws Exception {
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Question type not supported"));

        MvcResult result = mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_id_error_400",
                result.getResponse().getContentAsString());
    }

    @Test
    void updateQuestionInternalServerErrorMatchesContract() throws Exception {
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Could not send notification"));

        MvcResult result = mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false))))
                .andExpect(status().isInternalServerError())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "put_id_error_500",
                result.getResponse().getContentAsString());
    }
}
