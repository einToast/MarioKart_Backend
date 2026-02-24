package de.fsr.mariokart_backend.notification.service.pub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@Tag("unit")
class PublicNotificationReadServiceTest {

    @Test
    void getPublicKeyReturnsConfiguredValue() {
        PublicNotificationReadService service = new PublicNotificationReadService();
        ReflectionTestUtils.setField(service, "publicKey", "PUBLIC_KEY_VALUE");

        assertThat(service.getPublicKey()).isEqualTo("PUBLIC_KEY_VALUE");
    }
}
