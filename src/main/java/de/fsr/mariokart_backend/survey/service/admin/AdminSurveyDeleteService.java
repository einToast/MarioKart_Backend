package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "survey")
@CacheEvict(allEntries = true)
public class AdminSurveyDeleteService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final WebSocketService webSocketService;

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
        answerRepository.deleteAllByQuestionId(id);
        webSocketService.sendMessage("/topic/questions", "delete");
    }

    public void deleteAllQuestions() {
        questionRepository.deleteAll();
        answerRepository.deleteAll();
        webSocketService.sendMessage("/topic/questions", "delete");
    }

}
