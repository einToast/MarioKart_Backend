package de.fsr.mariokart_backend.survey.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.AnswerInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.AnswerReturnDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.AnswerRepository;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.AnswerInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.AnswerReturnDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SurveyService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final AnswerInputDTOService answerInputDTOService;
    private final AnswerReturnDTOService answerReturnDTOService;

    public QuestionReturnDTO getQuestion(Long id) {
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

    public QuestionReturnDTO createSurvey(QuestionInputDTO survey) {
        // return
        // questionRepository.save(questionInputDTOService.questionInputDTOToQuestion(survey));
        return questionReturnDTOService.questionToQuestionReturnDTO(
                questionRepository.save(questionInputDTOService.questionInputDTOToQuestion(survey)));
    }

    public AnswerReturnDTO submitAnswer(AnswerInputDTO answer) {
        // return
        // answerRepository.save(answerInputDTOService.answerInputDTOToAnswer(answer));
        Question question = questionRepository.findById(answer.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
        if (!question.getActive() || !question.getVisible()) {
            throw new IllegalArgumentException("Question is not active or visible.");
        }
        return answerReturnDTOService
                .answerToAnswerReturnDTO(answerRepository.save(answerInputDTOService.answerInputDTOToAnswer(answer)));
    }

    public QuestionReturnDTO updateQuestion(Long id, QuestionInputDTO question) {
        Question questionToUpdate = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
        Question updatedQuestion = questionInputDTOService.questionInputDTOToQuestion(question);
        if (updatedQuestion.getQuestionText() != null) {
            questionToUpdate.setQuestionText(updatedQuestion.getQuestionText());
        }
        if (updatedQuestion.getActive() != null) {
            questionToUpdate.setActive(updatedQuestion.getActive());
        }
        if (updatedQuestion.getVisible() != null) {
            questionToUpdate.setVisible(updatedQuestion.getVisible());
        }
        if (updatedQuestion.getLive() != null) {
            questionToUpdate.setLive(updatedQuestion.getLive());
        }
        if (updatedQuestion instanceof MultipleChoiceQuestion) {
            if (((MultipleChoiceQuestion) updatedQuestion).getOptions() != null) {
                ((MultipleChoiceQuestion) questionToUpdate)
                        .setOptions(((MultipleChoiceQuestion) updatedQuestion).getOptions());
            }
        } else if (updatedQuestion instanceof CheckboxQuestion) {
            if (((CheckboxQuestion) updatedQuestion).getOptions() != null) {
                ((CheckboxQuestion) questionToUpdate).setOptions(((CheckboxQuestion) updatedQuestion).getOptions());
            }
        } else if (updatedQuestion instanceof FreeTextQuestion) {
            // nothing to update
        } else if (updatedQuestion instanceof TeamQuestion) {
            if (((TeamQuestion) updatedQuestion).getFinalTeamsOnly() != null) {
                ((TeamQuestion) questionToUpdate)
                        .setFinalTeamsOnly(((TeamQuestion) updatedQuestion).getFinalTeamsOnly());
            }
            if (((TeamQuestion) updatedQuestion).getTeams() != null) {
                ((TeamQuestion) questionToUpdate).setTeams(((TeamQuestion) updatedQuestion).getTeams());
            }
        } else {
            throw new IllegalArgumentException("Question type not supported.");
        }
        return questionReturnDTOService.questionToQuestionReturnDTO(questionRepository.save(questionToUpdate));
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
        answerRepository.deleteAllByQuestionId(id);
    }

    public void deleteAllQuestions() {
        questionRepository.deleteAll();
        answerRepository.deleteAll();
    }
}
