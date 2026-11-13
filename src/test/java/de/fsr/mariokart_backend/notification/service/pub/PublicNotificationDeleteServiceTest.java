package de.fsr.mariokart_backend.notification.service.pub;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionEndpointDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicNotificationDeleteServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PublicNotificationDeleteService service;

    @Test
    void deleteSubscriptionRemovesEverySubscriptionOfTheEndpoint() {
        PushSubscription first = new PushSubscription();
        first.setId(1L);
        PushSubscription second = new PushSubscription();
        second.setId(2L);
        when(subscriptionRepository.findByEndpoint("https://example.test/1")).thenReturn(List.of(first, second));

        service.deleteSubscription(new PushSubscriptionEndpointDTO("https://example.test/1"));

        verify(subscriptionRepository).deleteAll(List.of(first, second));
    }

    @Test
    void deleteSubscriptionIgnoresRequestsWithoutEndpoint() {
        service.deleteSubscription(new PushSubscriptionEndpointDTO(null));
        service.deleteSubscription(null);

        verifyNoInteractions(subscriptionRepository);
    }
}
