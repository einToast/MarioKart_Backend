package de.fsr.mariokart_backend.match_plan.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient webClient() {
//        TODO: change to real url
        return WebClient.builder().baseUrl("http://localhost:8000").build();
    }
}
