package de.fsr.mariokart_backend.survey.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.config.CookieProperties;
import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.SurveyKeyRequiredException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerSubmissionResult;
import de.fsr.mariokart_backend.survey.service.pub.PublicSurveyCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import tools.jackson.core.JacksonException;

@WebMvcTest(PublicSurveyCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicSurveyCreateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicSurveyCreateService publicSurveyCreateService;

    @MockitoBean
    private CookieProperties cookieProperties;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void submitAnswerReturnsAnswer() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        AnswerReturnDTO response = new AnswerReturnDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenReturn(new AnswerSubmissionResult(response, null));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(1));
    }

    @Test
    void submitAnswerMapsConflict() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenThrow(new IllegalStateException("Question is not active or visible."));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }

    @Test
    void submitAnswerMapsTooManyRequests() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Maximum number of answers per team reached."));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(containsString("Maximum number of answers")));
    }

    @Test
    void submitAnswerMapsNotFound() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenThrow(new EntityNotFoundException("There is no question with this id."));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitAnswerMapsBadRequestForInvalidJsonCookie() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        JacksonException jacksonException = mock(JacksonException.class);
        when(jacksonException.getMessage()).thenReturn("Invalid JSON");
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenThrow(jacksonException);

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "not-json"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid JSON")));
    }

    @Test
    void submitAnswerSetsSurveyKeyCookieWhenKeyIssued() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        AnswerReturnDTO response = new AnswerReturnDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(cookieProperties.getPath()).thenReturn("/");
        when(cookieProperties.getSameSite()).thenReturn("Lax");
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), isNull()))
                .thenReturn(new AnswerSubmissionResult(response, "new-key"));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(cookie().value(PublicSurveyCreateController.SURVEY_KEY_COOKIE_NAME, "new-key"))
                .andExpect(cookie().httpOnly(PublicSurveyCreateController.SURVEY_KEY_COOKIE_NAME, true))
                .andExpect(cookie().maxAge(PublicSurveyCreateController.SURVEY_KEY_COOKIE_NAME, 2 * 24 * 60 * 60));
    }

    @Test
    void submitAnswerPassesSurveyKeyCookieAndSetsNoCookieWhenNoKeyIssued() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        AnswerReturnDTO response = new AnswerReturnDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), eq("known-key")))
                .thenReturn(new AnswerSubmissionResult(response, null));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"),
                                new MockCookie(PublicSurveyCreateController.SURVEY_KEY_COOKIE_NAME, "known-key"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
    }

    @Test
    void submitAnswerMapsForbiddenWhenSurveyKeyRequired() throws Exception {
        AnswerInputDTO input = new AnswerInputDTO(1L, "FREE_TEXT", "A", null, null, null);
        when(publicSurveyCreateService.submitAnswer(any(AnswerInputDTO.class), anyString(), any()))
                .thenThrow(new SurveyKeyRequiredException("A valid survey key is required to answer this question."));

        mockMvc.perform(post("/public/survey/answer")
                        .cookie(new MockCookie("user", "{\"teamId\":1}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("survey key is required")));
    }
}
