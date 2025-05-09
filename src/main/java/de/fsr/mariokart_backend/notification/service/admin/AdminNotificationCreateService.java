package de.fsr.mariokart_backend.notification.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminNotificationCreateService {

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSendService notificationSendService;

    public void sendNotificationToAll(String title, String message) throws NotificationNotSentException {
        List<PushSubscription> subscriptions = subscriptionRepository.findAll();
        try {
            for (PushSubscription subscription : subscriptions) {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "body", message));

                notificationSendService.sendNotification(subscription, payload);
            }
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }

    public void sendNotificationToTeam(Long teamId, String title, String message) throws NotificationNotSentException {
        List<PushSubscription> subscriptions = subscriptionRepository.findByTeamId(teamId);

        try {
            for (PushSubscription subscription : subscriptions) {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "body", message));

                notificationSendService.sendNotification(subscription, payload);
            }
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }
}