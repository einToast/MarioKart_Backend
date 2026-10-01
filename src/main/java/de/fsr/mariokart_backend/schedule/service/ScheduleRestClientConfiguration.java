package de.fsr.mariokart_backend.schedule.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ScheduleRestClientConfiguration {

    @Value("${SCHEDULE_PROTOCOL:http}")
    private String protocol;

    @Value("${SCHEDULE_HOST:localhost}")
    private String host;

    @Value("${SCHEDULE_PORT:8000}")
    private String port;

    @Bean
    public RestClient scheduleRestClient() {
        String baseUrl = "%s://%s:%s".formatted(protocol, host, port);
        log.info("baseUrl: {}", baseUrl);
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
