package de.fsr.mariokart_backend.notification.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionInputDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import de.fsr.mariokart_backend.notification.service.dto.NotificationInputDTOService;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicNotificationCreateServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @Mock
    private NotificationSendService notificationSendService;

    @Mock
    private NotificationInputDTOService notificationInputDTOService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PublicNotificationCreateService service;

    @Test
    void saveSubscriptionPersistsAndSendsTestNotification() throws Exception {
        PushSubscriptionInputDTO input = new PushSubscriptionInputDTO("https://example.test/1", "p256dh", "auth", 4L);
        PushSubscription incoming = subscription(null, 4L);

        when(notificationInputDTOService.pushSubscriptionInputDTOToPushSubscription(input)).thenReturn(incoming);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.saveSubscription(input);

        verify(notificationSendService).sendNotification(incoming, "payload");
        verify(subscriptionRepository).save(incoming);
    }

    @Test
    void saveSubscriptionDoesNotStoreUnreachableSubscriptions() throws Exception {
        PushSubscriptionInputDTO input = new PushSubscriptionInputDTO("https://example.test/1", "AQAB", "AQAB", 4L);
        PushSubscription incoming = subscription(null, 4L);

        when(notificationInputDTOService.pushSubscriptionInputDTOToPushSubscription(input)).thenReturn(incoming);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new IllegalArgumentException("Invalid point encoding"))
                .when(notificationSendService)
                .sendNotification(incoming, "payload");

        assertThatThrownBy(() -> service.saveSubscription(input))
                .isInstanceOf(NotificationNotSentException.class);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void saveSubscriptionUpdatesTheSubscriptionOfAKnownEndpoint() throws Exception {
        PushSubscriptionInputDTO input = new PushSubscriptionInputDTO("https://example.test/1", "new", "new", 9L);
        PushSubscription incoming = subscription(null, 9L);
        incoming.setEndpoint("https://example.test/1");
        incoming.setP256dh("new");
        PushSubscription stored = subscription(1L, 4L);
        stored.setEndpoint("https://example.test/1");
        PushSubscription duplicate = subscription(2L, 4L);
        duplicate.setEndpoint("https://example.test/1");

        when(notificationInputDTOService.pushSubscriptionInputDTOToPushSubscription(input)).thenReturn(incoming);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        when(subscriptionRepository.findByEndpoint("https://example.test/1")).thenReturn(List.of(stored, duplicate));

        service.saveSubscription(input);

        verify(subscriptionRepository).deleteAll(List.of(duplicate));
        verify(subscriptionRepository).save(stored);
        assertThat(stored.getTeamId()).isEqualTo(9L);
        assertThat(stored.getP256dh()).isEqualTo("new");
    }

    @Test
    void sendTestNotificationUsesFixedTitleAndBody() throws Exception {
        PushSubscription subscription = subscription(1L, 1L);
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendTestNotification(subscription);

        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.captor();
        verify(objectMapper).writeValueAsString(mapCaptor.capture());
        assertThat(mapCaptor.getValue()).containsEntry("title", "Mario Kart Turnier");
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
