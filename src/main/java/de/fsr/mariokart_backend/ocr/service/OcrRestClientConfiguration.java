package de.fsr.mariokart_backend.ocr.service;

import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OcrRestClientConfiguration {

    @Value("${OCR_PROTOCOL:http}")
    private String protocol;

    @Value("${OCR_HOST:localhost}")
    private String host;

    @Value("${OCR_PORT:8001}")
    private String port;

    @Bean
    RestClient ocrRestClient() {
        String baseUrl = "%s://%s:%s".formatted(protocol, host, port);
        log.info("OCR baseUrl: {}", baseUrl);
        // uvicorn drops the request body when the JDK client sends its default h2c upgrade headers
        HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
