package de.fsr.mariokart_backend.survey.controller.pub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;
import tools.jackson.core.JacksonException;

@WebMvcTest(PublicSurveyCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class PublicSurveyCreateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/public/survey/public-survey-create-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSurveyCreateService publicSurveyCreateService;

    @Test
    void submitAnswerSuccessMatchesContract() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        AnswerReturnDTO response = new AnswerReturnDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString())).thenReturn(response);

        MvcResult result = mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertJsonMatchesDefinition(SCHEMA, "post_answer_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void submitAnswerConflictMatchesContract() throws Exception {
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString()))
                .thenThrow(new IllegalStateException("Question is not active or visible."));

        MvcResult result = mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null))))
                .andExpect(status().isConflict())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_answer_error_409",
                result.getResponse().getContentAsString());
    }

    @Test
    void submitAnswerTooManyRequestsMatchesContract() throws Exception {
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString()))
                .thenThrow(new IllegalArgumentException("Maximum number of answers reached."));

        MvcResult result = mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null))))
                .andExpect(status().isTooManyRequests())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_answer_error_429",
                result.getResponse().getContentAsString());
    }

    @Test
    void submitAnswerNotFoundMatchesContract() throws Exception {
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString()))
                .thenThrow(new EntityNotFoundException("No question"));

        MvcResult result = mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null))))
                .andExpect(status().isNotFound())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_answer_error_404",
                result.getResponse().getContentAsString());
    }

    @Test
    void submitAnswerBadRequestMatchesContract() throws Exception {
        JacksonException jacksonException = mock(JacksonException.class);
        when(jacksonException.getMessage()).thenReturn("Invalid JSON");
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString()))
                .thenThrow(jacksonException);

        MvcResult result = mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "not-json"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null))))
                .andExpect(status().isBadRequest())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(SCHEMA, "post_answer_error_400",
                result.getResponse().getContentAsString());
    }
}
