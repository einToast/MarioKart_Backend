package de.fsr.mariokart_backend.survey.model.subclasses;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class CheckboxAnswerTest {

    @Test
    void getAnswerDetailsFormatsSelectedOptions() {
        CheckboxAnswer answer = new CheckboxAnswer();
        answer.setSelectedOptions(List.of(1, 3, 5));

        assertThat(answer.getAnswerDetails()).isEqualTo("Selected options: 1, 3, 5");
    }
}
