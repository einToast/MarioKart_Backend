package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class QuestionAnnouncementService {

    private final AdminNotificationCreateService adminNotificationCreateService;

    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void onQuestionOpened(QuestionOpenedEvent event) {
        try {
            adminNotificationCreateService.sendNotificationToAll("Neue Umfrage verfügbar!", event.questionText());
        } catch (NotificationNotSentException e) {
            log.warn("Could not announce question {}: {}", event.questionId(), e.getMessage());
        }
    }
}
