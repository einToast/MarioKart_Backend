package de.fsr.mariokart_backend.survey.service.admin;

import java.util.Map;

import de.fsr.mariokart_backend.survey.model.QuestionTranslation;
import de.fsr.mariokart_backend.translation.Translations;

public record QuestionOpenedEvent(Long questionId, String questionText, Map<String, QuestionTranslation> translations) {

    public String questionText(String language) {
        QuestionTranslation translation = translations == null ? null : translations.get(language);
        return translation == null ? questionText : Translations.translatedOrDefault(translation.questionText(), questionText);
    }
}
