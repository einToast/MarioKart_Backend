package de.fsr.mariokart_backend.notification.controller.pub;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionEndpointDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationDeleteService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicNotificationDeleteController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicNotificationDeleteControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationDeleteService publicNotificationDeleteService;

    @Test
    void unsubscribePassesTheEndpointToTheService() throws Exception {
        PushSubscriptionEndpointDTO subscription = new PushSubscriptionEndpointDTO("https://example.test/sub");

        mockMvc.perform(delete("/public/notification/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscription)))
                .andExpect(status().isOk());

        verify(publicNotificationDeleteService).deleteSubscription(subscription);
    }
}
