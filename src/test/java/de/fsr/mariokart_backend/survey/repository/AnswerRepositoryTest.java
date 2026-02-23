package de.fsr.mariokart_backend.survey.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextAnswer;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.testsupport.JpaSliceCacheConfig;
import de.fsr.mariokart_backend.testsupport.PostgresTestBase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
@Import(JpaSliceCacheConfig.class)
class AnswerRepositoryTest extends PostgresTestBase {

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findAndDeleteByQuestionIdWork() {
        FreeTextQuestion question = new FreeTextQuestion();
        question.setQuestionText("What do you improve?");
        question.setVisible(true);
        question.setActive(true);
        question.setLive(false);
        questionRepository.save(question);

        FreeTextAnswer first = new FreeTextAnswer();
        first.setQuestion(question);
        first.setTextAnswer("More tracks");

        FreeTextAnswer second = new FreeTextAnswer();
        second.setQuestion(question);
        second.setTextAnswer("Longer breaks");

        answerRepository.save(first);
        answerRepository.save(second);

        assertThat(answerRepository.findAllByQuestionId(question.getId())).hasSize(2);

        answerRepository.deleteAllByQuestionId(question.getId());

        assertThat(answerRepository.findAllByQuestionId(question.getId())).isEmpty();
    }
}
