package de.fsr.mariokart_backend.ocr.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.ocr.model.dto.ProviderReturnDTO;
import de.fsr.mariokart_backend.ocr.service.admin.AdminOcrReadService;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.OCR)
public class AdminOcrReadController {

    private final AdminOcrReadService adminOcrReadService;

    @GetMapping
    public List<String> getProviders() {
        return adminOcrReadService.getProviders();
    }

    @GetMapping("/available")
    public List<String> getAvailableProviders() {
        return adminOcrReadService.getAvailableProviders();
    }

    @GetMapping("/{providerName}/models")
    public List<String> getProviderModels(@PathVariable String providerName) {
        return adminOcrReadService.getProviderModels(providerName);
    }

    @GetMapping("/{providerName}/config")
    public ProviderReturnDTO getProviderConfig(@PathVariable String providerName) {
        return adminOcrReadService.getProviderConfig(providerName);
    }

}
