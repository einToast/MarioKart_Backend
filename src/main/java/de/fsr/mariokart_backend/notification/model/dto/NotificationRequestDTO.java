package de.fsr.mariokart_backend.notification.model.dto;

import java.util.Map;

import de.fsr.mariokart_backend.translation.Translations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationRequestDTO {
    
    private String title;
    private String message;
    private Map<String, NotificationTranslation> translations;

    public NotificationRequestDTO(String title, String message) {
        this(title, message, Map.of());
    }

    public NotificationRequestDTO in(String language) {
        NotificationTranslation translation = translations == null ? null : translations.get(language);
        if (translation == null) {
            return new NotificationRequestDTO(title, message);
        }
        return new NotificationRequestDTO(
                Translations.translatedOrDefault(translation.title(), title),
                Translations.translatedOrDefault(translation.message(), message));
    }
}
