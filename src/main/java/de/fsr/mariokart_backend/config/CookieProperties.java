package de.fsr.mariokart_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProperties {
    private boolean secure = true;
    private String sameSite = "Lax";
    private String path = "/";
}
