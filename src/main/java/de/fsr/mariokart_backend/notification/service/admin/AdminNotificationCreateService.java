package de.fsr.mariokart_backend.notification.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AdminNotificationCreateService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSendService notificationSendService;

    public void sendNotificationToAll(String title, String message) throws NotificationNotSentException {
        send(subscriptionRepository.findAll(), title, message);
    }

    public void sendNotificationToTeam(Long teamId, String title, String message) throws NotificationNotSentException {
        send(subscriptionRepository.findByTeamId(teamId), title, message);
    }

    private void send(List<PushSubscription> subscriptions, String title, String message)
            throws NotificationNotSentException {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "body", message));
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }

        int delivered = 0;
        Exception lastError = null;
        for (PushSubscription subscription : subscriptions) {
            try {
                int statusCode = notificationSendService.sendNotification(subscription, payload);
                if (NotificationSendService.isSubscriptionGone(statusCode)) {
                    subscriptionRepository.delete(subscription);
                } else {
                    delivered++;
                }
            } catch (Exception e) {
                log.warn("Could not send notification to subscription {}: {}", subscription.getId(), e.getMessage());
                lastError = e;
            }
        }

        if (lastError != null && delivered == 0) {
            throw new NotificationNotSentException("Could not send notification: " + lastError.getMessage());
        }
    }
}
