package de.fsr.mariokart_backend.survey.service.admin;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import de.fsr.mariokart_backend.exception.EntityNotFoundException;
import de.fsr.mariokart_backend.exception.NotificationNotSentException;
import de.fsr.mariokart_backend.notification.service.admin.AdminNotificationCreateService;
import de.fsr.mariokart_backend.survey.model.Question;
import de.fsr.mariokart_backend.survey.model.dto.QuestionInputDTO;
import de.fsr.mariokart_backend.survey.model.dto.QuestionReturnDTO;
import de.fsr.mariokart_backend.survey.model.subclasses.CheckboxQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.FreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.MultipleChoiceQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamOneFreeTextQuestion;
import de.fsr.mariokart_backend.survey.model.subclasses.TeamQuestion;
import de.fsr.mariokart_backend.survey.repository.QuestionRepository;
import de.fsr.mariokart_backend.survey.service.dto.QuestionInputDTOService;
import de.fsr.mariokart_backend.survey.service.dto.QuestionReturnDTOService;
import de.fsr.mariokart_backend.websocket.service.WebSocketService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "survey")
@CacheEvict(allEntries = true)
public class AdminSurveyUpdateService {
    private final QuestionRepository questionRepository;
    private final QuestionInputDTOService questionInputDTOService;
    private final QuestionReturnDTOService questionReturnDTOService;
    private final WebSocketService webSocketService;
    private final AdminNotificationCreateService adminNotificationCreateService;

    public QuestionReturnDTO updateQuestion(Long id, QuestionInputDTO question)
            throws EntityNotFoundException, NotificationNotSentException {
        Question questionToUpdate = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("There is no question with this id."));
        Question updatedQuestion = questionInputDTOService.questionInputDTOToQuestion(question);

        boolean questionCanBeAnswered = Boolean.TRUE.equals(updatedQuestion.getActive())
                && !Boolean.TRUE.equals(questionToUpdate.getActive())
                && Boolean.TRUE.equals(updatedQuestion.getVisible())
                && !Boolean.TRUE.equals(questionToUpdate.getVisible());

        updateCommonFields(questionToUpdate, updatedQuestion);
        updateTypeSpecificFields(questionToUpdate, updatedQuestion);

        Question savedQuestion = questionRepository.save(questionToUpdate);

        webSocketService.sendMessage("/topic/questions", "update");
        if (questionCanBeAnswered) {
            adminNotificationCreateService.sendNotificationToAll("Neue Umfrage verfügbar!",
                    questionToUpdate.getQuestionText());
        }
        return questionReturnDTOService.questionToQuestionReturnDTO(savedQuestion);
    }

    private void updateCommonFields(Question questionToUpdate, Question updatedQuestion) {
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
        questionToUpdate.setOneAnswerPerKey(updatedQuestion.isOneAnswerPerKey());
    }

    private void updateTypeSpecificFields(Question questionToUpdate, Question updatedQuestion) {
        switch (updatedQuestion) {
            case MultipleChoiceQuestion choiceQuestion -> {
                if (choiceQuestion.getOptions() != null) {
                    ((MultipleChoiceQuestion) questionToUpdate)
                            .setOptions(choiceQuestion.getOptions());
                }
            }
            case CheckboxQuestion checkboxQuestion -> {
                if (checkboxQuestion.getOptions() != null) {
                    ((CheckboxQuestion) questionToUpdate).setOptions(checkboxQuestion.getOptions());
                }
            }
            case TeamQuestion teamQuestion -> {
                if (teamQuestion.getFinalTeamsOnly() != null) {
                    ((TeamQuestion) questionToUpdate)
                            .setFinalTeamsOnly(teamQuestion.getFinalTeamsOnly());
                }
                if (teamQuestion.getTeams() != null) {
                    ((TeamQuestion) questionToUpdate).setTeams(teamQuestion.getTeams());
                }
            }
            case FreeTextQuestion _, TeamOneFreeTextQuestion _ -> {
                // nothing to update
            }
            default -> throw new IllegalArgumentException("Question type not supported.");
        }
    }
}
