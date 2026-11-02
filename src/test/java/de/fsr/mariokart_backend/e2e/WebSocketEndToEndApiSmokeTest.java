package de.fsr.mariokart_backend.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import de.fsr.mariokart_backend.e2e.testsupport.AbstractEndToEndApiSmokeTest;
import de.fsr.mariokart_backend.websocket.WebSocketConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Tag("integration")
@TestPropertySource(properties = {
        "app.user.secret-key=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
})
class WebSocketEndToEndApiSmokeTest extends AbstractEndToEndApiSmokeTest {

    @Autowired
    private SimpleBrokerMessageHandler simpleBrokerMessageHandler;

    @Test
    void brokerSendsAndExpectsHeartbeats() {
        assertThat(simpleBrokerMessageHandler.getHeartbeatValue())
                .containsExactly(WebSocketConfig.HEARTBEAT_MILLIS, WebSocketConfig.HEARTBEAT_MILLIS);
        assertThat(simpleBrokerMessageHandler.getTaskScheduler()).isNotNull();
    }
}
