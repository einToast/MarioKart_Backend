package de.fsr.mariokart_backend.notification.controller.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.NOTIFICATION)
public class PublicNotificationReadController {

    private final PublicNotificationReadService publicNotificationReadService;

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(publicNotificationReadService.getPublicKey());
    }

}
