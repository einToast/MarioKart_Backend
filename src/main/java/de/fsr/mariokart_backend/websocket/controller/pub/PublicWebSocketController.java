package de.fsr.mariokart_backend.websocket.controller.pub;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PublicWebSocketController {

    @MessageMapping("/public/sendMessages")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        log.info("Received message: {}", message);
        return "irgendwas";
    }
}