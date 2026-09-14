package de.fsr.mariokart_backend.notification.controller.admin;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MvcResult;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;
import de.fsr.mariokart_backend.testsupport.ContractSchemaSupport;

@WebMvcTest(AdminNotificationCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("contract")
class AdminNotificationCreateControllerContractTest extends AbstractWebMvcSliceTest {

    private static final String SCHEMA = "contracts/admin/notification/admin-notification-create-controller.schema.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminNotificationCreateService adminNotificationCreateService;

    @Test
    void sendToAllSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_send_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void sendToAllServiceUnavailableMatchesContract() throws Exception {
        doThrow(new NotificationNotSentException("Failed to send"))
                .when(adminNotificationCreateService)
                .sendNotificationToAll(anyString(), anyString());

        MvcResult result = mockMvc.perform(post("/admin/notification/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isServiceUnavailable())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_send_error_503",
                result.getResponse().getContentAsString());
    }

    @Test
    void sendToTeamSuccessMatchesContract() throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/notification/send/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isOk())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_send_teamid_success",
                result.getResponse().getContentAsString());
    }

    @Test
    void sendToTeamServiceUnavailableMatchesContract() throws Exception {
        doThrow(new NotificationNotSentException("Downstream unavailable"))
                .when(adminNotificationCreateService)
                .sendNotificationToTeam(anyLong(), anyString(), anyString());

        MvcResult result = mockMvc.perform(post("/admin/notification/send/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body())))
                .andExpect(status().isServiceUnavailable())
                .andReturn();

        ContractSchemaSupport.assertStringMatchesDefinition(
                SCHEMA,
                "post_send_teamid_error_503",
                result.getResponse().getContentAsString());
    }

    private static NotificationRequestDTO body() {
        return new NotificationRequestDTO("Round", "Starts");
    }
}
