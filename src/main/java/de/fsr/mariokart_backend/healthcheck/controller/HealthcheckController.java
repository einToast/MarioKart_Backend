package de.fsr.mariokart_backend.healthcheck.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/healthcheck")
@AllArgsConstructor
public class HealthcheckController {
    @GetMapping
    public ResponseEntity<String> getHealthcheck() {
        return ResponseEntity.ok("OK");
    }

}
