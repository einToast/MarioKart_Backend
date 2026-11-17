package de.fsr.mariokart_backend.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Component
@AllArgsConstructor
public class TranslationRebase {

    private static final String TRANSLATIONS = "translations";

    private final ObjectMapper objectMapper;

    public String rebaseJson(String json, String from, String to) {
        if (json == null || json.isBlank()) {
            return json;
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JacksonException _) {
            return json;
        }
        rebaseTree(root, from, to);
        return objectMapper.writeValueAsString(root);
    }

    public <T> ObjectNode toNode(Map<String, Object> fields, Map<String, T> translations) {
        ObjectNode node = objectMapper.createObjectNode();
        fields.forEach((field, value) -> node.set(field, objectMapper.valueToTree(value)));
        if (translations != null) {
            node.set(TRANSLATIONS, objectMapper.valueToTree(translations));
        }
        return node;
    }

    public <T> Map<String, T> translationsOf(ObjectNode node, TypeReference<Map<String, T>> type) {
        JsonNode translations = node.get(TRANSLATIONS);
        return translations == null ? new HashMap<>() : objectMapper.convertValue(translations, type);
    }

    public String text(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    public List<String> texts(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || !value.isArray()) {
            return List.of();
        }
        List<String> texts = new ArrayList<>();
        value.forEach(entry -> texts.add(entry.isNull() ? null : entry.asString()));
        return texts;
    }

    public void rebaseTree(JsonNode node, String from, String to) {
        if (node instanceof ObjectNode object) {
            if (object.get(TRANSLATIONS) instanceof ObjectNode translations) {
                rebaseObject(object, translations, from, to);
            }
            object.properties().stream()
                    .filter(property -> !TRANSLATIONS.equals(property.getKey()))
                    .forEach(property -> rebaseTree(property.getValue(), from, to));
        } else if (node instanceof ArrayNode array) {
            array.forEach(child -> rebaseTree(child, from, to));
        }
    }

    private void rebaseObject(ObjectNode object, ObjectNode translations, String from, String to) {
        ObjectNode previous = objectMapper.createObjectNode();
        if (translations.get(to) instanceof ObjectNode target) {
            for (Map.Entry<String, JsonNode> translated : target.properties()) {
                String field = translated.getKey();
                JsonNode base = object.get(field);
                JsonNode value = translated.getValue();
                if (hasText(value) && base != null && base.isString()) {
                    previous.set(field, base);
                    object.set(field, value);
                } else if (value.isArray() && base != null && base.isArray()) {
                    rebaseList(object, previous, field, (ArrayNode) base, (ArrayNode) value);
                }
            }
        }
        translations.remove(to);
        translations.remove(from);
        if (!previous.isEmpty()) {
            translations.set(from, previous);
        }
        if (translations.isEmpty()) {
            object.remove(TRANSLATIONS);
        }
    }

    private void rebaseList(ObjectNode object, ObjectNode previous, String field, ArrayNode base, ArrayNode translated) {
        ArrayNode rebased = objectMapper.createArrayNode();
        ArrayNode replaced = objectMapper.createArrayNode();
        boolean anyReplaced = false;
        for (int index = 0; index < base.size(); index++) {
            JsonNode value = index < translated.size() ? translated.get(index) : null;
            if (hasText(value)) {
                rebased.add(value);
                replaced.add(base.get(index));
                anyReplaced = true;
            } else {
                rebased.add(base.get(index));
                replaced.add("");
            }
        }
        if (anyReplaced) {
            object.set(field, rebased);
            previous.set(field, replaced);
        }
    }

    private static boolean hasText(JsonNode value) {
        return value != null && value.isString() && !value.asString().isBlank();
    }
}
