package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "survey")
@CacheEvict(allEntries = true)
public class AdminSurveyDeleteService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
        answerRepository.deleteAllByQuestionId(id);
    }

    public void deleteAllQuestions() {
        questionRepository.deleteAll();
        answerRepository.deleteAll();
    }

}
