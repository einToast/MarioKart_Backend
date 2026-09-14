package de.fsr.mariokart_backend.survey.model.subclasses;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class MultipleChoiceAnswerTest {

    @Test
    void getAnswerDetailsReturnsSelectedOption() {
        MultipleChoiceAnswer answer = new MultipleChoiceAnswer();
        answer.setSelectedOption(2);

        assertThat(answer.getAnswerDetails()).isEqualTo("Selected option: 2");
    }
}
