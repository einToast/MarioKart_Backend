package de.fsr.mariokart_backend.ocr.service.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import de.fsr.mariokart_backend.ocr.model.dto.ProviderInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.ProviderReturnDTO;
import lombok.AllArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@AllArgsConstructor
public class AdminOcrUpdateService {

    private final RestClient ocrRestClient;
    private final ObjectMapper objectMapper;

    public ProviderReturnDTO updateProviderConfig(String providerName, ProviderInputDTO providerInputDTO) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", providerInputDTO.model());
        requestBody.put("thinking", providerInputDTO.thinking());

        String response;
        try {
            response = ocrRestClient.put()
                    .uri("/providers/{providerName}/configure", providerName)
                    .body(requestBody)
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