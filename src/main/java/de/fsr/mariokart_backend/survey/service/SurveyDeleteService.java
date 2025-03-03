package de.fsr.mariokart_backend.survey.service;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SurveyDeleteService {
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