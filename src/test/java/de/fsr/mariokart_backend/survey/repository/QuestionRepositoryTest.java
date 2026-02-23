package de.fsr.mariokart_backend.survey.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class QuestionRepositoryTest extends PostgresTestBase {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findAllByVisibleWorks() {
        MultipleChoiceQuestion visible = new MultipleChoiceQuestion();
        visible.setQuestionText("Visible question");
        visible.setVisible(true);
        visible.setActive(true);
        visible.setLive(false);
        visible.setOptions(List.of("A", "B"));

        MultipleChoiceQuestion hidden = new MultipleChoiceQuestion();
        hidden.setQuestionText("Hidden question");
        hidden.setVisible(false);
        hidden.setActive(true);
        hidden.setLive(false);
        hidden.setOptions(List.of("A", "B"));

        questionRepository.save(visible);
        questionRepository.save(hidden);

        assertThat(questionRepository.findAllByVisible(true)).hasSize(1);
        assertThat(questionRepository.findAllByVisible(false)).hasSize(1);
    }
}
