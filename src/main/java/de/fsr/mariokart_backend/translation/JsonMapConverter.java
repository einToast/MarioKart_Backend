package de.fsr.mariokart_backend.translation;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.AttributeConverter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public abstract class JsonMapConverter<V> implements AttributeConverter<Map<String, V>, String> {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final TypeReference<Map<String, V>> type;

    protected JsonMapConverter(TypeReference<Map<String, V>> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(Map<String, V> attribute) {
        return attribute == null || attribute.isEmpty() ? null : MAPPER.writeValueAsString(attribute);
    }

    @Override
    public Map<String, V> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new HashMap<>();
        }
        return new HashMap<>(MAPPER.readValue(dbData, type));
    }
}
