package de.fsr.mariokart_backend.websocket.controller.pub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Tag("unit")
class PublicWebSocketControllerTest {

    @Test
    void handleMessageReturnsStaticResponse() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        PublicWebSocketController controller = new PublicWebSocketController(template);

        String result = controller.handleMessage("hello");

        assertThat(result).isEqualTo("irgendwas");
        verifyNoInteractions(template);
    }
}
