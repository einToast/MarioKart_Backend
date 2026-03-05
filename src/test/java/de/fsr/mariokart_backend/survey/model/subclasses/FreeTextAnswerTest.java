package de.fsr.mariokart_backend.survey.model.subclasses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class FreeTextAnswerTest {

    @Test
    void getAnswerDetailsReturnsTextResponse() {
        FreeTextAnswer answer = new FreeTextAnswer();
        answer.setTextAnswer("Great tournament");

        assertThat(answer.getAnswerDetails()).isEqualTo("Text response: Great tournament");
    }
}
