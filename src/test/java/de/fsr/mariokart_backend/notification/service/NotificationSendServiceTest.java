package de.fsr.mariokart_backend.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.notification.model.PushSubscription;

@Tag("unit")
class NotificationSendServiceTest {

    @Test
    void constructorRegistersBouncyCastleProvider() {
        new NotificationSendService();

        assertThat(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)).isNotNull();
    }

    @Test
    void sendNotificationThrowsForInvalidSubscriptionKeyData() {
        NotificationSendService service = new NotificationSendService();

        PushSubscription subscription = new PushSubscription();
        subscription.setEndpoint("https://example.com/push");
        subscription.setP256dh("AQAB");
        subscription.setAuth("AQAB");

        assertThatThrownBy(() -> service.sendNotification(subscription, "{\"title\":\"Hello\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid point encoding");
    }
}
