package de.fsr.mariokart_backend.notification.service.pub;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionInputDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import de.fsr.mariokart_backend.notification.service.NotificationTextService;
import de.fsr.mariokart_backend.notification.service.dto.NotificationInputDTOService;
import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicNotificationCreateService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final NotificationSendService notificationSendService;
    private final NotificationInputDTOService notificationInputDTOService;

    private final ObjectMapper objectMapper;
    private final NotificationTextService notificationTextService;

    public void saveSubscription(PushSubscriptionInputDTO subscriptionInput) throws NotificationNotSentException {
        PushSubscription incoming = notificationInputDTOService
                .pushSubscriptionInputDTOToPushSubscription(subscriptionInput);
        sendTestNotification(incoming);

        List<PushSubscription> existing = subscriptionRepository.findByEndpoint(incoming.getEndpoint());
        PushSubscription subscription = existing.isEmpty() ? incoming : existing.getFirst();
        if (existing.size() > 1) {
            subscriptionRepository.deleteAll(existing.subList(1, existing.size()));
        }
        subscription.setP256dh(incoming.getP256dh());
        subscription.setAuth(incoming.getAuth());
        subscription.setTeamId(incoming.getTeamId());
        subscription.setLanguage(incoming.getLanguage());
        subscriptionRepository.save(subscription);
    }

    public void sendTestNotification(PushSubscription subscription) throws NotificationNotSentException {
        int statusCode;
        try {
            String language = Translations.languageOrDefault(subscription.getLanguage());
            String payload = objectMapper
                    .writeValueAsString(Map.of(
                        "title", notificationTextService.text("notification.test.title", language),
                        "body", notificationTextService.text("notification.test.body", language),
                        "lang", language,
                        "actions", Map.of(
                                "open", notificationTextService.text("notification.action.open", language),
                                "close", notificationTextService.text("notification.action.close", language))));
            statusCode = notificationSendService.sendNotification(subscription, payload);
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
        if (NotificationSendService.isSubscriptionGone(statusCode)) {
            throw new NotificationNotSentException("Could not send notification: subscription is gone");
        }
    }
}