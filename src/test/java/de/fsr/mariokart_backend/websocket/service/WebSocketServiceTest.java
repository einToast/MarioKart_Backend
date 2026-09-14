package de.fsr.mariokart_backend.websocket.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketService service;

    @Test
    void sendMessageDelegatesToMessagingTemplate() {
        service.sendMessage("/topic/messages", "hello");

        verify(messagingTemplate).convertAndSend("/topic/messages", "hello");
    }
}
