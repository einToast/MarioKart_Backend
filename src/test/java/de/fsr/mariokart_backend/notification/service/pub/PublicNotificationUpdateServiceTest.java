package de.fsr.mariokart_backend.notification.service.pub;

import static org.assertj.core.api.Assertions.assertThat;
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
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionLanguageDTO;
import de.fsr.mariokart_backend.notification.repository.PushSubscriptionRepository;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PublicNotificationUpdateServiceTest {

    @Mock
    private PushSubscriptionRepository subscriptionRepository;

    @InjectMocks
    private PublicNotificationUpdateService service;

    @Test
    void updateLanguageChangesEverySubscriptionOfTheEndpoint() {
        PushSubscription first = subscription(1L);
        PushSubscription second = subscription(2L);
        when(subscriptionRepository.findByEndpoint("https://example.test/1")).thenReturn(List.of(first, second));

        service.updateLanguage(new PushSubscriptionLanguageDTO("https://example.test/1", "en"));

        assertThat(first.getLanguage()).isEqualTo("en");
        assertThat(second.getLanguage()).isEqualTo("en");
        verify(subscriptionRepository).saveAll(List.of(first, second));
    }

    @Test
    void updateLanguageFallsBackToGermanForUnknownLanguages() {
        PushSubscription subscription = subscription(1L);
        when(subscriptionRepository.findByEndpoint("https://example.test/1")).thenReturn(List.of(subscription));

        service.updateLanguage(new PushSubscriptionLanguageDTO("https://example.test/1", "<script>"));

        assertThat(subscription.getLanguage()).isEqualTo("de");
    }

    @Test
    void updateLanguageIgnoresRequestsWithoutEndpoint() {
        service.updateLanguage(new PushSubscriptionLanguageDTO(null, "en"));
        service.updateLanguage(null);

        verifyNoInteractions(subscriptionRepository);
    }

    private static PushSubscription subscription(Long id) {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(id);
        subscription.setTeamId(1L);
        subscription.setEndpoint("https://example.test/1");
        return subscription;
    }
}
