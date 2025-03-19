package de.fsr.mariokart_backend.controller.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ControllerType {
    HEALTHCHECK("/healthcheck"),
    REGISTRATION("/teams"),
    SCHEDULE("/schedule"),
    SETTINGS("/settings"),
    SURVEY("/survey"),
    USER("/user");

    private final String path;
}
