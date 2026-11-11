package de.fsr.mariokart_backend.notification.service.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import de.fsr.mariokart_backend.notification.model.LocalizedNotification;
import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.model.dto.NotificationTranslation;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;
import de.fsr.mariokart_backend.notification.service.NotificationSendService;
import de.fsr.mariokart_backend.notification.service.NotificationTextService;
import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AdminNotificationCreateService {

    private static final int MAX_PARALLEL_SENDS = 8;
    public static final int MAX_TRANSLATED_TITLE_LENGTH = 255;
    public static final int MAX_TRANSLATED_MESSAGE_LENGTH = 1000;

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;
    private final NotificationSendService notificationSendService;
    private final NotificationTextService notificationTextService;

    public void sendNotificationToAll(NotificationRequestDTO notification) throws NotificationNotSentException {
        sendNotificationToAll(withValidTranslations(notification)::in);
    }

    public void sendNotificationToTeam(Long teamId, NotificationRequestDTO notification)
            throws NotificationNotSentException {
        sendNotificationToTeam(teamId, withValidTranslations(notification)::in);
    }

    public void sendNotificationToAll(LocalizedNotification notification) throws NotificationNotSentException {
        deliver(subscriptionRepository.findAll(), subscription -> notification);
    }

    public void sendNotificationToTeam(Long teamId, LocalizedNotification notification)
            throws NotificationNotSentException {
        deliver(subscriptionRepository.findByTeamId(teamId), subscription -> notification);
    }

    public void sendNotificationsToTeams(Map<Long, LocalizedNotification> notificationsByTeam)
            throws NotificationNotSentException {
        deliver(subscriptionRepository.findByTeamIdIn(notificationsByTeam.keySet()),
                subscription -> notificationsByTeam.get(subscription.getTeamId()));
    }

    private NotificationRequestDTO withValidTranslations(NotificationRequestDTO notification) {
        return new NotificationRequestDTO(notification.getTitle(), notification.getMessage(),
                Translations.sanitize(notification.getTranslations(), translation -> {
                    String title = Translations.text(translation.title(), MAX_TRANSLATED_TITLE_LENGTH, "titles");
                    String message = Translations.text(translation.message(), MAX_TRANSLATED_MESSAGE_LENGTH, "messages");
                    return title == null && message == null ? null : new NotificationTranslation(title, message);
                }));
    }

    private String toPayload(LocalizedNotification notification, String language) throws NotificationNotSentException {
        NotificationRequestDTO localized = notification.in(language);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", localized.getTitle());
        payload.put("body", localized.getMessage());
        payload.put("lang", language);
        payload.put("actions", Map.of(
                "open", notificationTextService.text("notification.action.open", language),
                "close", notificationTextService.text("notification.action.close", language)));
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new NotificationNotSentException("Could not send notification: " + e.getMessage());
        }
    }

    private void deliver(List<PushSubscription> subscriptions, Function<PushSubscription, LocalizedNotification> notificationOf)
            throws NotificationNotSentException {
        if (subscriptions.isEmpty()) {
            return;
        }
        Map<LocalizedNotification, Map<String, String>> payloads = new HashMap<>();
        List<String> payloadPerSubscription = new ArrayList<>();
        for (PushSubscription subscription : subscriptions) {
            LocalizedNotification notification = notificationOf.apply(subscription);
            String language = Translations.languageOrDefault(subscription.getLanguage());
            Map<String, String> byLanguage = payloads.computeIfAbsent(notification, key -> new HashMap<>());
            String payload = byLanguage.get(language);
            if (payload == null) {
                payload = toPayload(notification, language);
                byLanguage.put(language, payload);
            }
            payloadPerSubscription.add(payload);
        }

        List<Future<Integer>> statusCodes = new ArrayList<>();
        try (ExecutorService executor = Executors
                .newFixedThreadPool(Math.min(MAX_PARALLEL_SENDS, subscriptions.size()))) {
            for (int i = 0; i < subscriptions.size(); i++) {
                PushSubscription subscription = subscriptions.get(i);
                String payload = payloadPerSubscription.get(i);
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
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                throw new NotificationNotSentException("Could not send notification: interrupted");
            }
        }

        if (lastError != null && delivered == 0) {
            throw new NotificationNotSentException("Could not send notification: " + lastError.getMessage());
        }
    }
}
