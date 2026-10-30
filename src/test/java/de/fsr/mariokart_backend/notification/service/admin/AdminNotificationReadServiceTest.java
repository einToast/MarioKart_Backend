package de.fsr.mariokart_backend.notification.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminNotificationReadServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private AdminNotificationReadService service;

    @Test
    void getSubscriptionCountReturnsRepositoryCount() {
        when(subscriptionRepository.count()).thenReturn(7L);

        assertThat(service.getSubscriptionCount()).isEqualTo(7L);
    }

    @Test
    void getSubscriptionCountReturnsZeroWithoutSubscriptions() {
        when(subscriptionRepository.count()).thenReturn(0L);

        assertThat(service.getSubscriptionCount()).isZero();
    }
}
