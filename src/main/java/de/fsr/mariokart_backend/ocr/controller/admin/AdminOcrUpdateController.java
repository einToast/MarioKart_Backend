package de.fsr.mariokart_backend.ocr.controller.admin;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import de.fsr.mariokart_backend.ocr.model.dto.ProviderInputDTO;
import de.fsr.mariokart_backend.ocr.model.dto.ProviderReturnDTO;
import de.fsr.mariokart_backend.ocr.service.admin.AdminOcrUpdateService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.ADMIN, controllerType = ControllerType.OCR)
public class AdminOcrUpdateController {

    private final AdminOcrUpdateService adminOcrUpdateService;

    @PutMapping("/{providerName}/config")
    public ProviderReturnDTO putMethodName(@PathVariable String providerName,
            @RequestBody ProviderInputDTO providerInputDTO) {
        return adminOcrUpdateService.updateProviderConfig(providerName, providerInputDTO);
    }

}