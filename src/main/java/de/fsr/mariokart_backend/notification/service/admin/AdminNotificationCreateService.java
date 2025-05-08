package de.fsr.mariokart_backend.notification.service.admin;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationCreateService.class);

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSendService notificationSendService;

    public void sendNotificationToAll(String title, String message) throws NotificationNotSentException {
        logger.info("Sende Benachrichtigung an alle: {}", message);
        List<PushSubscription> subscriptions = subscriptionRepository.findAll();
        logger.info("Gefundene Subscriptions: {}", subscriptions.size());
        try {
            for (PushSubscription subscription : subscriptions) {
                logger.info("Nachricht für Subscription {} ist kein JSON, erstelle JSON-Objekt", subscription.getId());
                String payload = objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "body", message));

                logger.info("Sende Nachricht an Subscription {}: {}", subscription.getId(), payload);
                notificationSendService.sendNotification(subscription, payload);
            }
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }

    public void sendNotificationToTeam(Long teamId, String title, String message) throws NotificationNotSentException {
        logger.info("Sende Benachrichtigung an Team {}: {}", teamId, message);
        List<PushSubscription> subscriptions = subscriptionRepository.findByTeamId(teamId);
        logger.info("Gefundene Subscriptions für Team {}: {}", teamId, subscriptions.size());

        try {
            for (PushSubscription subscription : subscriptions) {
                logger.info("Nachricht für Team {} an Subscription {} ist kein JSON, erstelle JSON-Objekt", teamId,
                        subscription.getId());
                String payload = objectMapper.writeValueAsString(Map.of(
                        "title", title,
                        "body", message));

                logger.info("Sende Nachricht an Subscription {} für Team {}: {}", subscription.getId(), teamId,
                        payload);

                notificationSendService.sendNotification(subscription, payload);
            }
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }
}