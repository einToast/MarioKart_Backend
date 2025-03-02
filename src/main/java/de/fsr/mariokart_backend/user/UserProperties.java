package de.fsr.mariokart_backend.user;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.user")
@Getter
@Setter
public class UserProperties {
    private int expiresAfter;
    private String secretKey;
}