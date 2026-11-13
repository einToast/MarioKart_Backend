package de.fsr.mariokart_backend.notification.service.admin;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminNotificationDeleteServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private AdminNotificationDeleteService service;

    @Test
    void deleteAllSubscriptionsEmptiesTheRepository() {
        service.deleteAllSubscriptions();

        verify(subscriptionRepository).deleteAll();
    }
}
