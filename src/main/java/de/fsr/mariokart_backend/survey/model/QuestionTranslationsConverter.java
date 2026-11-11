package de.fsr.mariokart_backend.survey.model;

import java.util.Map;

import de.fsr.mariokart_backend.translation.JsonMapConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;

@Converter
public class QuestionTranslationsConverter extends JsonMapConverter<QuestionTranslation> {

    public QuestionTranslationsConverter() {
        super(new TypeReference<Map<String, QuestionTranslation>>() {
        });
    }
}
