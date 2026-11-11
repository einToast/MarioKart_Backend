package de.fsr.mariokart_backend.survey.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Map;

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
import de.fsr.mariokart_backend.notification.model.LocalizedNotification;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.NotificationTextService;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.survey.model.QuestionTranslation;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class QuestionAnnouncementServiceTest {

    @Mock
    private AdminNotificationCreateService adminNotificationCreateService;

    @Spy
    private NotificationTextService notificationTextService = new NotificationTextService();

    @Captor
    private ArgumentCaptor<LocalizedNotification> notificationCaptor;

    @InjectMocks
    private QuestionAnnouncementService service;

    @Test
    void onQuestionOpenedAnnouncesTheQuestionToEveryoneInTheirLanguage() throws NotificationNotSentException {
        service.onQuestionOpened(new QuestionOpenedEvent(3L, "Wer gewinnt?",
                Map.of("en", new QuestionTranslation("Who wins?", null))));

        verify(adminNotificationCreateService).sendNotificationToAll(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().in("de"))
                .isEqualTo(new NotificationRequestDTO("Neue Umfrage verfügbar!", "Wer gewinnt?"));
        assertThat(notificationCaptor.getValue().in("en"))
                .isEqualTo(new NotificationRequestDTO("New survey available!", "Who wins?"));
    }

    @Test
    void onQuestionOpenedFallsBackToTheGermanQuestionWithoutTranslation() throws NotificationNotSentException {
        service.onQuestionOpened(new QuestionOpenedEvent(3L, "Wer gewinnt?", Map.of()));

        verify(adminNotificationCreateService).sendNotificationToAll(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().in("en"))
                .isEqualTo(new NotificationRequestDTO("New survey available!", "Wer gewinnt?"));
    }

    @Test
    void onQuestionOpenedKeepsGoingWhenNoNotificationArrives() throws NotificationNotSentException {
        doThrow(new NotificationNotSentException("push service down"))
                .when(adminNotificationCreateService).sendNotificationToAll(any(LocalizedNotification.class));

        service.onQuestionOpened(new QuestionOpenedEvent(3L, "New Poll", Map.of()));

        verify(adminNotificationCreateService).sendNotificationToAll(any(LocalizedNotification.class));
    }
}
