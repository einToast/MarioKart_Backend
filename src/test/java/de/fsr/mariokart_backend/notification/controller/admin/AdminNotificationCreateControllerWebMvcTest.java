package de.fsr.mariokart_backend.notification.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(AdminNotificationCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class AdminNotificationCreateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminNotificationCreateService adminNotificationCreateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void sendNotificationToAllReturnsOk() throws Exception {
        NotificationRequestDTO body = new NotificationRequestDTO("Round", "Starts");

        mockMvc.perform(post("/admin/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void sendNotificationToAllReturnsServiceUnavailableOnFailure() throws Exception {
        NotificationRequestDTO body = new NotificationRequestDTO("Round", "Starts");
        doThrow(new NotificationNotSentException("Failed to send"))
                .when(adminNotificationCreateService)
                .sendNotificationToAll(anyString(), anyString());

        mockMvc.perform(post("/admin/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Failed to send")));
    }

    @Test
    void sendNotificationToTeamReturnsOk() throws Exception {
        NotificationRequestDTO body = new NotificationRequestDTO("Round", "Starts");

        mockMvc.perform(post("/admin/notification/send/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    void sendNotificationToTeamReturnsServiceUnavailableOnFailure() throws Exception {
        NotificationRequestDTO body = new NotificationRequestDTO("Round", "Starts");
        doThrow(new NotificationNotSentException("Downstream unavailable"))
                .when(adminNotificationCreateService)
                .sendNotificationToTeam(anyLong(), anyString(), anyString());

        mockMvc.perform(post("/admin/notification/send/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Downstream unavailable")));
    }
}
