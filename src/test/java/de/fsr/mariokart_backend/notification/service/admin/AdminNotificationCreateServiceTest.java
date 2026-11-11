package de.fsr.mariokart_backend.notification.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.model.dto.NotificationTranslation;
import de.fsr.mariokart_backend.notification.service.NotificationTextService;
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

    @Spy
    private NotificationTextService notificationTextService = new NotificationTextService();

    @Captor
    private ArgumentCaptor<Map<String, Object>> payloadCaptor;

    @InjectMocks
    private AdminNotificationCreateService service;

    @Test
    void sendNotificationToAllSendsToEachSubscription() throws Exception {
        PushSubscription first = subscription(1L, 1L);
        PushSubscription second = subscription(2L, 2L);

        when(subscriptionRepository.findAll()).thenReturn(List.of(first, second));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendNotificationToAll(new NotificationRequestDTO("Title", "Body"));

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

        assertThatThrownBy(() -> service.sendNotificationToAll(new NotificationRequestDTO("Title", "Body")))
                .isInstanceOf(NotificationNotSentException.class)
                .hasMessageContaining("Could not send notification");
    }

    @Test
    void sendNotificationToAllKeepsSendingWhenOneSubscriptionFails() throws Exception {
        PushSubscription broken = subscription(1L, 1L);
        PushSubscription working = subscription(2L, 2L);

        when(subscriptionRepository.findAll()).thenReturn(List.of(broken, working));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new IllegalArgumentException("Invalid point encoding"))
                .when(notificationSendService)
                .sendNotification(eq(broken), anyString());
        when(notificationSendService.sendNotification(working, "payload")).thenReturn(201);

        service.sendNotificationToAll(new NotificationRequestDTO("Title", "Body"));

        verify(notificationSendService).sendNotification(working, "payload");
    }

    @Test
    void sendNotificationToAllRemovesSubscriptionsThePushServiceDropped() throws Exception {
        PushSubscription gone = subscription(1L, 1L);
        PushSubscription working = subscription(2L, 2L);

        when(subscriptionRepository.findAll()).thenReturn(List.of(gone, working));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        when(notificationSendService.sendNotification(gone, "payload")).thenReturn(410);
        when(notificationSendService.sendNotification(working, "payload")).thenReturn(201);

        service.sendNotificationToAll(new NotificationRequestDTO("Title", "Body"));

        verify(subscriptionRepository).delete(gone);
        verify(subscriptionRepository, never()).delete(working);
    }

    @Test
    void sendNotificationToTeamSendsOnlyTeamSubscriptions() throws Exception {
        PushSubscription first = subscription(1L, 5L);
        PushSubscription second = subscription(2L, 5L);

        when(subscriptionRepository.findByTeamId(5L)).thenReturn(List.of(first, second));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");

        service.sendNotificationToTeam(5L, new NotificationRequestDTO("Round", "Starts now"));

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

        assertThatThrownBy(() -> service.sendNotificationToTeam(7L, new NotificationRequestDTO("T", "M")))
                .isInstanceOf(NotificationNotSentException.class)
                .hasMessageContaining("Could not send notification");
    }

    @Test
    void sendNotificationsToTeamsSendsEveryTeamItsOwnNotification() throws Exception {
        PushSubscription playing = subscription(1L, 5L);
        PushSubscription pausing = subscription(2L, 6L);

        when(subscriptionRepository.findByTeamIdIn(Set.of(5L, 6L))).thenReturn(List.of(playing, pausing));
        when(objectMapper.writeValueAsString(argThat(payload -> payload != null && "Switch 1".equals(((Map<?, ?>) payload).get("title")))))
                .thenReturn("play");
        when(objectMapper.writeValueAsString(argThat(payload -> payload != null && "Pause".equals(((Map<?, ?>) payload).get("title")))))
                .thenReturn("pause");

        service.sendNotificationsToTeams(Map.of(
                5L, new NotificationRequestDTO("Switch 1", "Go")::in,
                6L, new NotificationRequestDTO("Pause", "Rest")::in));

        verify(notificationSendService).sendNotification(playing, "play");
        verify(notificationSendService).sendNotification(pausing, "pause");
    }

    @Test
    void sendNotificationsToTeamsWrapsErrorsWhenNothingArrives() throws Exception {
        PushSubscription subscription = subscription(1L, 5L);

        when(subscriptionRepository.findByTeamIdIn(Set.of(5L))).thenReturn(List.of(subscription));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("payload");
        doThrow(new RuntimeException("downstream"))
                .when(notificationSendService)
                .sendNotification(eq(subscription), anyString());

        assertThatThrownBy(() -> service.sendNotificationsToTeams(Map.of(5L, new NotificationRequestDTO("T", "M")::in)))
                .isInstanceOf(NotificationNotSentException.class)
                .hasMessageContaining("downstream");
    }

    @Test
    void sendNotificationToAllSendsEverySubscriptionTheTextsOfItsLanguage() throws Exception {
        PushSubscription german = subscription(1L, 1L);
        PushSubscription english = subscription(2L, 2L);
        english.setLanguage("en");
        PushSubscription unknown = subscription(3L, 3L);
        unknown.setLanguage("xx");

        when(subscriptionRepository.findAll()).thenReturn(List.of(german, english, unknown));
        when(objectMapper.writeValueAsString(anyMap())).thenReturn("de-payload", "en-payload", "xx-payload");

        service.sendNotificationToAll(new NotificationRequestDTO("Hallo", "Text",
                Map.of("en", new NotificationTranslation("Hello", " "))));

        verify(objectMapper, times(3)).writeValueAsString(payloadCaptor.capture());
        assertThat(payloadCaptor.getAllValues()).containsExactly(
                Map.of("title", "Hallo", "body", "Text", "lang", "de",
                        "actions", Map.of("open", "Öffnen", "close", "Schließen")),
                Map.of("title", "Hello", "body", "Text", "lang", "en",
                        "actions", Map.of("open", "Open", "close", "Close")),
                Map.of("title", "Hallo", "body", "Text", "lang", "xx",
                        "actions", Map.of("open", "Öffnen", "close", "Schließen")));
        verify(notificationSendService).sendNotification(german, "de-payload");
        verify(notificationSendService).sendNotification(english, "en-payload");
        verify(notificationSendService).sendNotification(unknown, "xx-payload");
    }

    @Test
    void sendNotificationToAllRejectsInvalidTranslations() {
        NotificationRequestDTO invalidLanguage = new NotificationRequestDTO("T", "M",
                Map.of("English", new NotificationTranslation("T", "M")));
        NotificationRequestDTO tooLong = new NotificationRequestDTO("T", "M",
                Map.of("en", new NotificationTranslation("x".repeat(256), "M")));

        assertThatThrownBy(() -> service.sendNotificationToAll(invalidLanguage)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.sendNotificationToAll(tooLong)).isInstanceOf(IllegalArgumentException.class);
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
