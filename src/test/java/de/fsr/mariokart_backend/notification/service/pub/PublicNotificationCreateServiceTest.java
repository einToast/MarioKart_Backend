package de.fsr.mariokart_backend.notification.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicNotificationCreateServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationSendService notificationSendService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PublicNotificationCreateService service;

    @Test
    void saveSubscriptionPersistsAndSendsTestNotification() throws Exception {
        PushSubscription incoming = subscription(1L, 4L);
        PushSubscription saved = subscription(2L, 4L);

        when(subscriptionRepository.save(incoming)).thenReturn(saved);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.saveSubscription(incoming);

        verify(subscriptionRepository).save(incoming);
        verify(notificationSendService).sendNotification(saved, "payload");
    }

    @Test
    void sendTestNotificationUsesFixedTitleAndBody() throws Exception {
        PushSubscription subscription = subscription(1L, 1L);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendTestNotification(subscription);

        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(objectMapper).writeValueAsString(mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("title")).isEqualTo("Mario Kart Turnier");
        assertThat(mapCaptor.getValue().get("body")).contains("Testbenachrichtigung");
    }

    @Test
    void sendTestNotificationWrapsAnySendError() throws Exception {
        PushSubscription subscription = subscription(1L, 1L);

        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new RuntimeException("send failed"))
                .when(notificationSendService)
                .sendNotification(subscription, "payload");

        assertThatThrownBy(() -> service.sendTestNotification(subscription))
                .isInstanceOf(NotificationNotSentException.class)
                .hasMessageContaining("Could not send notification");
    }

    private static PushSubscription subscription(Long id, Long teamId) {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(id);
        subscription.setTeamId(teamId);
        subscription.setEndpoint("https://example.test/" + id);
        subscription.setP256dh("p256dh");
        subscription.setAuth("auth");
        return subscription;
    }
}
