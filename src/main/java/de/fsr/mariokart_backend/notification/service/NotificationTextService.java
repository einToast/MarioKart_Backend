package de.fsr.mariokart_backend.notification.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.translation.Translations;

@Service
public class NotificationTextService {

    private final ResourceBundleMessageSource messages = new ResourceBundleMessageSource();

    public NotificationTextService() {
        messages.setBasename("i18n/notifications");
        messages.setDefaultEncoding(StandardCharsets.UTF_8.name());
        messages.setFallbackToSystemLocale(false);
        messages.setDefaultLocale(Locale.forLanguageTag(Translations.DEFAULT_LANGUAGE));
        messages.setAlwaysUseMessageFormat(true);
    }

    public String text(String key, String language, Object... arguments) {
        return messages.getMessage(key, arguments, Locale.forLanguageTag(Translations.languageOrDefault(language)));
    }
}
