package de.fsr.mariokart_backend.settings.model;

import java.util.Map;

import de.fsr.mariokart_backend.translation.JsonMapConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

@Converter
public class SwitchTranslationsConverter extends JsonMapConverter<SwitchTranslation> {

    public SwitchTranslationsConverter() {
        super(new TypeReference<Map<String, SwitchTranslation>>() {
        });
    }
}
