package de.fsr.mariokart_backend.controller.annotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RestType {
    GET("/get"),
    POST("/post"),
    PUT("/put"),
    DELETE("/delete");

    private final String path;
}
