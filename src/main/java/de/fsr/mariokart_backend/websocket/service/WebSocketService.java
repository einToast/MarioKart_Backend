package de.fsr.mariokart_backend.websocket.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessage(String topic, String message) {
        log.info("{}: {}", topic, message);
        messagingTemplate.convertAndSend(topic, message);
    }
}
