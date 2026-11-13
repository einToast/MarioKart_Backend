package de.fsr.mariokart_backend.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.seed")
@Getter
@Setter
public class SeedProperties {
    private String event;
}
