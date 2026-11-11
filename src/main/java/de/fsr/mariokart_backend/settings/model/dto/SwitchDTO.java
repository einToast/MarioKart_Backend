package de.fsr.mariokart_backend.settings.model.dto;

import java.util.Map;

import de.fsr.mariokart_backend.settings.model.SwitchTranslation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SwitchDTO {
    private String name;
    private String color;
    private Map<String, SwitchTranslation> translations;
}
