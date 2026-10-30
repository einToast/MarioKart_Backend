package de.fsr.mariokart_backend.notification.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationReadService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.NOTIFICATION)
public class AdminNotificationReadController {

    private final AdminNotificationReadService adminNotificationReadService;

    @GetMapping("/subscriptions/count")
    public long getSubscriptionCount() {
        return adminNotificationReadService.getSubscriptionCount();
    }
}
