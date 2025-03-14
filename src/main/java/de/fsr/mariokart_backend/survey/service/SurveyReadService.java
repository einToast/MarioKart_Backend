package de.fsr.mariokart_backend.survey.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SurveyReadService {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;

    public QuestionReturnDTO getQuestion(Long id) throws EntityNotFoundException {
        return questionReturnDTOService.questionToQuestionReturnDTO(questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id.")));
    }

    public List<QuestionReturnDTO> getQuestions() {
        return questionRepository.findAll().stream()
                .map(questionReturnDTOService::questionToQuestionReturnDTO)
                .toList();
    }

    public List<QuestionReturnDTO> getVisibleQuestions() {
        return questionRepository.findAllByVisible(true).stream()
                .map(questionReturnDTOService::questionToQuestionReturnDTO)
                .toList();
    }

    public List<AnswerReturnDTO> getAnswersOfQuestion(Long id) {
        return answerRepository.findAllByQuestionId(id).stream()
                .map(answerReturnDTOService::answerToAnswerReturnDTO)
                .toList();
    }
}