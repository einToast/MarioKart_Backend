package de.fsr.mariokart_backend.survey.service.admin;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class AdminSurveyDeleteServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private AdminSurveyDeleteService service;

    @Test
    void deleteQuestionRemovesQuestionAndRelatedAnswers() {
        service.deleteQuestion(5L);

        verify(questionRepository).deleteById(5L);
        verify(answerRepository).deleteAllByQuestionId(5L);
        verify(webSocketService).sendMessage("/topic/questions", "delete");
    }

    @Test
    void deleteAllQuestionsRemovesAllQuestionsAndAnswers() {
        service.deleteAllQuestions();

        verify(questionRepository).deleteAll();
        verify(answerRepository).deleteAll();
        verify(webSocketService).sendMessage("/topic/questions", "delete");
    }
}
