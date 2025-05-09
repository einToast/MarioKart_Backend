package de.fsr.mariokart_backend.notification.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.NOTIFICATION)
public class AdminNotificationCreateController {

    private final AdminNotificationCreateService adminNotificationCreateService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendNotificationToAll(@RequestBody NotificationRequestDTO notification) {
        try {
            adminNotificationCreateService.sendNotificationToAll(notification.getTitle(), notification.getMessage());
            return ResponseEntity.ok().build();
        } catch (NotificationNotSentException e) {
            return ResponseEntity.status(503).build();
        }
    }

    @PostMapping("/send/{teamId}")
    public ResponseEntity<Void> sendNotificationToTeam(@PathVariable Long teamId, @RequestBody NotificationRequestDTO notification) {
        try {
            adminNotificationCreateService.sendNotificationToTeam(teamId, notification.getTitle(), notification.getMessage());
            return ResponseEntity.ok().build();
        } catch (NotificationNotSentException e) {
            return ResponseEntity.status(503).build();
        }
    }

}
