package de.fsr.mariokart_backend.settings.model;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SwitchConfig {

    private String name;

    private String color;

    @Convert(converter = SwitchTranslationsConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<String, SwitchTranslation> translations = new HashMap<>();

    public SwitchConfig(String name, String color) {
        this(name, color, new HashMap<>());
    }
}
