package de.fsr.mariokart_backend.websocket.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class PublicWebSocketControllerTest {

    @Test
    void handleMessageReturnsStaticResponse() {
        PublicWebSocketController controller = new PublicWebSocketController();

        String result = controller.handleMessage("hello");

        assertThat(result).isEqualTo("irgendwas");
    }
}
