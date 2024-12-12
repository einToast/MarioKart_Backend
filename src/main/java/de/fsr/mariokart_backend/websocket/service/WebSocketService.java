package de.fsr.mariokart_backend.websocket.service;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(String topic, String message) {
        System.out.println(topic + ": " + message);
        messagingTemplate.convertAndSend(topic, message);
    }
}
