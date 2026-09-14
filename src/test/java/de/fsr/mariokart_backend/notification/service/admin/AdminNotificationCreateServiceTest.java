package de.fsr.mariokart_backend.notification.service.admin;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AdminNotificationCreateServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NotificationSendService notificationSendService;

    @InjectMocks
    private AdminNotificationCreateService service;

    @Test
    void sendNotificationToAllSendsToEachSubscription() throws Exception {
        PushSubscription first = subscription(1L, 1L);
        PushSubscription second = subscription(2L, 2L);

        when(subscriptionRepository.findAll()).thenReturn(List.of(first, second));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendNotificationToAll("Title", "Body");

        verify(notificationSendService).sendNotification(first, "payload");
        verify(notificationSendService).sendNotification(second, "payload");
    }

    @Test
    void sendNotificationToAllWrapsUnderlyingErrors() throws Exception {
        PushSubscription subscription = subscription(1L, 1L);

        when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new RuntimeException("boom"))
                .when(notificationSendService)
                .sendNotification(eq(subscription), anyString());

        assertThatThrownBy(() -> service.sendNotificationToAll("Title", "Body"))
                .isInstanceOf(NotificationNotSentException.class)
                .hasMessageContaining("Could not send notification");
    }

    @Test
    void sendNotificationToTeamSendsOnlyTeamSubscriptions() throws Exception {
        PushSubscription first = subscription(1L, 5L);
        PushSubscription second = subscription(2L, 5L);

        when(subscriptionRepository.findByTeamId(5L)).thenReturn(List.of(first, second));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendNotificationToTeam(5L, "Round", "Starts now");

        verify(notificationSendService).sendNotification(first, "payload");
        verify(notificationSendService).sendNotification(second, "payload");
    }

    @Test
    void sendNotificationToTeamWrapsErrors() throws Exception {
        PushSubscription subscription = subscription(1L, 7L);

        when(subscriptionRepository.findByTeamId(7L)).thenReturn(List.of(subscription));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new RuntimeException("downstream"))
                .when(notificationSendService)
                .sendNotification(eq(subscription), anyString());

        assertThatThrownBy(() -> service.sendNotificationToTeam(7L, "T", "M"))
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
