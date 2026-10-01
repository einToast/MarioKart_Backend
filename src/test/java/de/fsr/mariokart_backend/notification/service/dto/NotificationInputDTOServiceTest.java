package de.fsr.mariokart_backend.notification.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.notification.model.PushSubscription;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionInputDTO;

@Tag("unit")
class NotificationInputDTOServiceTest {

    private final NotificationInputDTOService service = new NotificationInputDTOService();

    @Test
    void pushSubscriptionInputDTOToPushSubscriptionCopiesFieldsWithoutId() {
        PushSubscriptionInputDTO input = new PushSubscriptionInputDTO("https://example.test/sub", "p256dh", "auth", 4L);

        PushSubscription subscription = service.pushSubscriptionInputDTOToPushSubscription(input);

        assertThat(subscription.getId()).isNull();
        assertThat(subscription.getEndpoint()).isEqualTo("https://example.test/sub");
        assertThat(subscription.getP256dh()).isEqualTo("p256dh");
        assertThat(subscription.getAuth()).isEqualTo("auth");
        assertThat(subscription.getTeamId()).isEqualTo(4L);
    }
}
