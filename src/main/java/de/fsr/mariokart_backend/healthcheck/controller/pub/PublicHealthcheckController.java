package de.fsr.mariokart_backend.healthcheck.controller.pub;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fsr.mariokart_backend.controller.annotation.ApiController;
import de.fsr.mariokart_backend.controller.annotation.ApiType;
import de.fsr.mariokart_backend.controller.annotation.ControllerType;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@ApiController(apiType = ApiType.PUBLIC, controllerType = ControllerType.HEALTHCHECK)
public class PublicHealthcheckController {

    @GetMapping
    public ResponseEntity<String> getHealthcheck() {
        return ResponseEntity.ok("OK");
    }
}