package de.fsr.mariokart_backend.survey.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.service.admin.AdminSurveyUpdateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminSurveyUpdateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminSurveyUpdateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminSurveyUpdateService adminSurveyUpdateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void updateQuestionReturnsUpdatedQuestion() throws Exception {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        QuestionReturnDTO response = new QuestionReturnDTO(1L, "FREE_TEXT", "Q", List.of(), true, true, false, false);
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class))).thenReturn(response);

        mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateQuestionMapsNotFound() throws Exception {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new EntityNotFoundException("There is no question with this id."));

        mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("question")));
    }

    @Test
    void updateQuestionMapsBadRequest() throws Exception {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new IllegalArgumentException("Question type not supported."));

        mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Question type")));
    }

    @Test
    void updateQuestionMapsInternalServerError() throws Exception {
        QuestionInputDTO input = new QuestionInputDTO("Q", "FREE_TEXT", List.of(), true, true, false, false);
        when(adminSurveyUpdateService.updateQuestion(anyLong(), any(QuestionInputDTO.class)))
                .thenThrow(new NotificationNotSentException("Could not send notification"));

        mockMvc.perform(put("/admin/survey/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Could not send notification")));
    }
}
