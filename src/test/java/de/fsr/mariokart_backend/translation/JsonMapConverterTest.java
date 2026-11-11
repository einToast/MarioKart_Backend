package de.fsr.mariokart_backend.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.fsr.mariokart_backend.settings.model.SwitchTranslation;
import de.fsr.mariokart_backend.settings.model.SwitchTranslationsConverter;
import de.fsr.mariokart_backend.survey.model.QuestionTranslation;
import de.fsr.mariokart_backend.survey.model.QuestionTranslationsConverter;

@Tag("unit")
class JsonMapConverterTest {

    private final QuestionTranslationsConverter questionConverter = new QuestionTranslationsConverter();
    private final SwitchTranslationsConverter switchConverter = new SwitchTranslationsConverter();

    @Test
    void storesAndRestoresQuestionTranslations() {
        Map<String, QuestionTranslation> translations = Map.of("en", new QuestionTranslation("Who wins?", List.of("", "Blue")));

        String column = questionConverter.convertToDatabaseColumn(translations);

        assertThat(questionConverter.convertToEntityAttribute(column)).isEqualTo(translations);
    }

    @Test
    void storesAndRestoresSwitchTranslations() {
        Map<String, SwitchTranslation> translations = Map.of("en", new SwitchTranslation("Blue"));

        assertThat(switchConverter.convertToEntityAttribute(switchConverter.convertToDatabaseColumn(translations)))
                .isEqualTo(translations);
    }

    @Test
    void storesNoColumnWithoutTranslationsAndReadsAnEmptyMapBack() {
        assertThat(switchConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(switchConverter.convertToDatabaseColumn(Map.of())).isNull();
        assertThat(switchConverter.convertToEntityAttribute(null)).isEmpty();
        assertThat(switchConverter.convertToEntityAttribute(" ")).isEmpty();
    }
}
