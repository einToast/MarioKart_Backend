package de.fsr.mariokart_backend.notification.model;

import de.fsr.mariokart_backend.notification.model.dto.NotificationRequestDTO;

@FunctionalInterface
public interface LocalizedNotification {

    NotificationRequestDTO in(String language);
}
