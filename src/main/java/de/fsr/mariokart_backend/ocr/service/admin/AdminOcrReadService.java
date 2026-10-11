package de.fsr.mariokart_backend.ocr.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import de.fsr.mariokart_backend.ocr.model.dto.ProviderReturnDTO;
import lombok.AllArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class AdminOcrReadService {

    private final RestClient ocrRestClient;
    private final ObjectMapper objectMapper;

    public List<String> getProviders() {
        String response;
        try {
            response = ocrRestClient.get()
                    .uri("/providers")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to ocr service", e);
        }

        try {
            return objectMapper.readValue(response, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public List<String> getAvailableProviders() {
        String response;
        try {
            response = ocrRestClient.get()
                    .uri("/providers/available")
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to ocr service", e);
        }

        try {
            return objectMapper.readValue(response, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public List<String> getProviderModels(String providerName) {
        String response;
        try {
            response = ocrRestClient.get()
                    .uri("/providers/{providerName}/models", providerName)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to ocr service", e);
        }
        try {
            return objectMapper.readValue(response, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

    public ProviderReturnDTO getProviderConfig(String providerName) {
        String response;
        try {
            response = ocrRestClient.get()
                    .uri("/providers/{providerName}/config", providerName)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send request to ocr service", e);
        }

        try {
            return objectMapper.readValue(response, ProviderReturnDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON response", e);
        }
    }

}
