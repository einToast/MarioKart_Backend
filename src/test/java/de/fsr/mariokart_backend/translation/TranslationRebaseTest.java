package de.fsr.mariokart_backend.translation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Tag("unit")
class TranslationRebaseTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final TranslationRebase rebase = new TranslationRebase(objectMapper);

    private String rebased(String json) {
        return rebase.rebaseJson(json, "de", "en");
    }

    @Test
    void swapsTranslatedTextsWithTheBaseTexts() {
        assertThat(rebased("""
                {"entries":[{"time":"10:00","text":"Begrüßung","translations":{"en":{"text":"Welcome"}}}]}"""))
                .isEqualTo("""
                        {"entries":[{"time":"10:00","text":"Welcome","translations":{"de":{"text":"Begrüßung"}}}]}""");
    }

    @Test
    void keepsUntranslatedTextsAsTheyAre() {
        assertThat(rebased("""
                [{"text":"Pause"},{"text":"Essen","translations":{"en":{"text":" "}}}]"""))
                .isEqualTo("""
                        [{"text":"Pause"},{"text":"Essen"}]""");
    }

    @Test
    void swapsListsEntryByEntry() {
        assertThat(rebased("""
                {"options":["Rot","Blau","Grün"],"translations":{"en":{"options":["","Blue"]}}}"""))
                .isEqualTo("""
                        {"options":["Rot","Blue","Grün"],"translations":{"de":{"options":["","Blau",""]}}}""");
    }

    @Test
    void keepsTranslationsIntoOtherLanguages() {
        assertThat(rebase.rebaseJson("""
                {"text":"Pause","translations":{"en":{"text":"Break"},"fr":{"text":"Pause"}}}""", "de", "en"))
                .isEqualTo("""
                        {"text":"Break","translations":{"fr":{"text":"Pause"},"de":{"text":"Pause"}}}""");
    }

    @Test
    void switchingBackRestoresTheOriginal() {
        String original = """
                {"text":"Begrüßung","options":["Ja","Nein"],"translations":{"en":{"text":"Welcome","options":["Yes"]}}}""";

        String back = rebase.rebaseJson(rebased(original), "en", "de");

        assertThat(objectMapper.readTree(back)).isEqualTo(objectMapper.readTree("""
                {"text":"Begrüßung","options":["Ja","Nein"],"translations":{"en":{"text":"Welcome","options":["Yes",""]}}}"""));
    }

    @Test
    void leavesTextThatIsNoJsonUntouched() {
        assertThat(rebased("not json")).isEqualTo("not json");
        assertThat(rebased(null)).isNull();
    }
}
