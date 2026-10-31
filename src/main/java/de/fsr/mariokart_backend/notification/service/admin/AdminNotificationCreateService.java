package de.fsr.mariokart_backend.notification.service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AdminNotificationCreateService {

    private static final int MAX_PARALLEL_SENDS = 8;

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSendService notificationSendService;

    public void sendNotificationToAll(String title, String message) throws NotificationNotSentException {
        send(subscriptionRepository.findAll(), title, message);
    }

    public void sendNotificationToTeam(Long teamId, String title, String message) throws NotificationNotSentException {
        send(subscriptionRepository.findByTeamId(teamId), title, message);
    }

    public void sendNotificationsToTeams(Map<Long, NotificationRequestDTO> notificationsByTeam)
            throws NotificationNotSentException {
        Map<Long, String> payloadsByTeam = new HashMap<>();
        for (Map.Entry<Long, NotificationRequestDTO> entry : notificationsByTeam.entrySet()) {
            payloadsByTeam.put(entry.getKey(), toPayload(entry.getValue().getTitle(), entry.getValue().getMessage()));
        }
        deliver(subscriptionRepository.findByTeamIdIn(notificationsByTeam.keySet()),
                subscription -> payloadsByTeam.get(subscription.getTeamId()));
    }

    private void send(List<PushSubscription> subscriptions, String title, String message)
            throws NotificationNotSentException {
        String payload = toPayload(title, message);
        deliver(subscriptions, subscription -> payload);
    }

    private String toPayload(String title, String message) throws NotificationNotSentException {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "title", title,
                    "body", message));
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }

    private void deliver(List<PushSubscription> subscriptions, Function<PushSubscription, String> payloadOf)
            throws NotificationNotSentException {
        if (subscriptions.isEmpty()) {
            return;
        }

        List<Future<Integer>> statusCodes = new ArrayList<>();
        try (ExecutorService executor = Executors
                .newFixedThreadPool(Math.min(MAX_PARALLEL_SENDS, subscriptions.size()))) {
            for (PushSubscription subscription : subscriptions) {
                String payload = payloadOf.apply(subscription);
                statusCodes.add(executor.submit(() -> notificationSendService.sendNotification(subscription, payload)));
            }
        }

        int delivered = 0;
        Throwable lastError = null;
        for (int i = 0; i < subscriptions.size(); i++) {
            PushSubscription subscription = subscriptions.get(i);
            try {
                int statusCode = statusCodes.get(i).get();
                if (NotificationSendService.isSubscriptionGone(statusCode)) {
                    subscriptionRepository.delete(subscription);
                } else {
                    delivered++;
                }
            } catch (ExecutionException e) {
                log.warn("Could not send notification to subscription {}: {}", subscription.getId(),
                        e.getCause().getMessage());
                lastError = e.getCause();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NotificationNotSentException("Could not send notification: interrupted");
            }
        }

        if (lastError != null && delivered == 0) {
            throw new NotificationNotSentException("Could not send notification: " + lastError.getMessage());
        }
    }
}
