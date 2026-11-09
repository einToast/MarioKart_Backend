package de.fsr.mariokart_backend.translation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class Translations {

    public static final int MAX_LANGUAGES = 10;

    private static final Pattern LANGUAGE_CODE = Pattern.compile("[a-z]{2,3}(-[A-Za-z0-9]{2,8})?");

    private Translations() {
    }

    public static <T> Map<String, T> sanitize(Map<String, T> translations, UnaryOperator<T> sanitizer) {
        Map<String, T> sanitized = new LinkedHashMap<>();
        if (translations == null) {
            return sanitized;
        }
        if (translations.size() > MAX_LANGUAGES) {
            throw new IllegalArgumentException("There can be translations for at most %d languages.".formatted(MAX_LANGUAGES));
        }
        translations.forEach((language, translation) -> {
            if (language == null || !LANGUAGE_CODE.matcher(language).matches()) {
                throw new IllegalArgumentException("Translations need a language code like \"en\".");
            }
            T kept = translation == null ? null : sanitizer.apply(translation);
            if (kept != null) {
                sanitized.put(language, kept);
            }
        });
        return sanitized;
    }

    public static String text(String text, int maxLength, String field) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException("Translated %s can have at most %d characters.".formatted(field, maxLength));
        }
        return trimmed;
    }
}
