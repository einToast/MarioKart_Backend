package de.fsr.mariokart_backend.notification.controller.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.notification.model.dto.PushSubscriptionEndpointDTO;
import de.fsr.mariokart_backend.notification.service.pub.PublicNotificationDeleteService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.NOTIFICATION)
public class PublicNotificationDeleteController {

    private final PublicNotificationDeleteService publicNotificationDeleteService;

    @DeleteMapping("/subscription")
    public ResponseEntity<Void> unsubscribe(@RequestBody PushSubscriptionEndpointDTO subscription) {
        publicNotificationDeleteService.deleteSubscription(subscription);
        return ResponseEntity.ok().build();
    }
}
