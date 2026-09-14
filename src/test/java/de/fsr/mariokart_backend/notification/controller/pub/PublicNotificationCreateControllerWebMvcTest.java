package de.fsr.mariokart_backend.notification.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationCreateService;
import de.fsr.mariokart_backend.testsupport.AbstractWebMvcSliceTest;

@WebMvcTest(PublicNotificationCreateController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Tag("web")
class PublicNotificationCreateControllerWebMvcTest extends AbstractWebMvcSliceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicNotificationCreateService publicNotificationCreateService;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void subscribeReturnsOk() throws Exception {
        PushSubscription subscription = subscription();

        mockMvc.perform(post("/public/notification/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscription)))
                .andExpect(status().isOk());
    }

    @Test
    void subscribeReturnsServiceUnavailableOnFailure() throws Exception {
        PushSubscription subscription = subscription();
        doThrow(new NotificationNotSentException("Could not send notification"))
                .when(publicNotificationCreateService)
                .saveSubscription(any(PushSubscription.class));

        mockMvc.perform(post("/public/notification/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subscription)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("Could not send notification")));
    }

    private static PushSubscription subscription() {
        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint("https://example.test/sub");
        subscription.setP256dh("p256dh");
        subscription.setAuth("auth");
        subscription.setTeamId(1L);
        return subscription;
    }
}
