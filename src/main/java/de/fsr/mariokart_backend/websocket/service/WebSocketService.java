package de.fsr.mariokart_backend.websocket.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(String topic, String message) {
        System.out.println(topic + ": " + message);
        messagingTemplate.convertAndSend(topic, message);
    }
}
