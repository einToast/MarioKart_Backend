package de.fsr.mariokart_backend.survey.service;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SurveyCreateService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final AnswerInputDTOService answerInputDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;

    public QuestionReturnDTO createSurvey(QuestionInputDTO survey) {
        return questionReturnDTOService.questionToQuestionReturnDTO(
                questionRepository.save(questionInputDTOService.questionInputDTOToQuestion(survey)));
    }

    public AnswerReturnDTO submitAnswer(AnswerInputDTO answer) throws EntityNotFoundException {
        Question question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
        if (!question.getActive() || !question.getVisible()) {
            throw new IllegalArgumentException("Question is not active or visible.");
        }
        return answerReturnDTOService
                .answerToAnswerReturnDTO(answerRepository.save(answerInputDTOService.answerInputDTOToAnswer(answer)));
    }
} 