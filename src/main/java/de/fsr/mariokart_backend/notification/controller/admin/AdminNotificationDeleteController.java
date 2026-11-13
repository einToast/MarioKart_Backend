package de.fsr.mariokart_backend.notification.controller.admin;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationDeleteService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.NOTIFICATION)
public class AdminNotificationDeleteController {

    private final AdminNotificationDeleteService adminNotificationDeleteService;

    @DeleteMapping("/subscriptions")
    public void deleteAllSubscriptions() {
        adminNotificationDeleteService.deleteAllSubscriptions();
    }
}
