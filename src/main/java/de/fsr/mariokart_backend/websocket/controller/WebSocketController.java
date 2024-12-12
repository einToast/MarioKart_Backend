package de.fsr.mariokart_backend.websocket.controller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin
@AllArgsConstructor
@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sendMessages")  // Hier sendet der Client Nachrichten
    @SendTo("/topic/messages")  // Hierhin wird die Nachricht an die verbundenen Clients gesendet
    public String handleMessage(String message) {
        System.out.println("Received message: " + message);
//        return message; // Einfacher Echo-Server
        return "irgendwas";  // Einfacher Echo-Server
    }

}
