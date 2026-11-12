package de.fsr.mariokart_backend.notification.controller.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionLanguageDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.NOTIFICATION)
public class PublicNotificationUpdateController {

    private final PublicNotificationUpdateService publicNotificationUpdateService;

    @PutMapping("/subscription/language")
    public ResponseEntity<Void> updateLanguage(@RequestBody PushSubscriptionLanguageDTO update) {
        publicNotificationUpdateService.updateLanguage(update);
        return ResponseEntity.ok().build();
    }
}
