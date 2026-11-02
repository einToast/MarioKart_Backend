package de.fsr.mariokart_backend.survey.service.admin;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class QuestionAnnouncementServiceTest {

    @Mock
    private AdminNotificationCreateService adminNotificationCreateService;

    @InjectMocks
    private QuestionAnnouncementService service;

    @Test
    void onQuestionOpenedAnnouncesTheQuestionToEveryone() throws NotificationNotSentException {
        service.onQuestionOpened(new QuestionOpenedEvent(3L, "New Poll"));

        verify(adminNotificationCreateService).sendNotificationToAll("Neue Umfrage verfügbar!", "New Poll");
    }

    @Test
    void onQuestionOpenedKeepsGoingWhenNoNotificationArrives() throws NotificationNotSentException {
        doThrow(new NotificationNotSentException("push service down"))
                .when(adminNotificationCreateService).sendNotificationToAll("Neue Umfrage verfügbar!", "New Poll");

        service.onQuestionOpened(new QuestionOpenedEvent(3L, "New Poll"));

        verify(adminNotificationCreateService).sendNotificationToAll("Neue Umfrage verfügbar!", "New Poll");
    }
}
