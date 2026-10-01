package de.fsr.mariokart_backend.notification.service.pub;

import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionInputDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import de.fsr.mariokart_backend.notification.service.dto.NotificationInputDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PublicNotificationCreateService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final NotificationSendService notificationSendService;
    private final NotificationInputDTOService notificationInputDTOService;

    private final ObjectMapper objectMapper;

    public void saveSubscription(PushSubscriptionInputDTO subscriptionInput) throws NotificationNotSentException {
        PushSubscription sub = subscriptionRepository.save(
                notificationInputDTOService.pushSubscriptionInputDTOToPushSubscription(subscriptionInput));
        sendTestNotification(sub);
    }

    public void sendTestNotification(PushSubscription subscription) throws NotificationNotSentException {
        try {
            String payload = objectMapper
                    .writeValueAsString(Map.of(
                        "title", "Mario Kart Turnier",
                        "body", "Das ist eine Testbenachrichtigung!"));
            notificationSendService.sendNotification(subscription, payload);
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }
}