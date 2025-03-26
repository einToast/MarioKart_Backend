package de.fsr.mariokart_backend.controller.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ApiType {
    ADMIN("/admin"),
    PUBLIC("/public");

    private final String path;
}
