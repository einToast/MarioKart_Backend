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

        boolean questionCanBeAnswered = updatedQuestion.getActive() == true
                && updatedQuestion.getActive() != questionToUpdate.getActive() && updatedQuestion.getVisible() == true
                && updatedQuestion.getVisible() != questionToUpdate.getVisible();

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
        if (updatedQuestion instanceof MultipleChoiceQuestion choiceQuestion) {
            if (choiceQuestion.getOptions() != null) {
                ((MultipleChoiceQuestion) questionToUpdate)
                        .setOptions(choiceQuestion.getOptions());
            }
        } else if (updatedQuestion instanceof CheckboxQuestion checkboxQuestion) {
            if (checkboxQuestion.getOptions() != null) {
                ((CheckboxQuestion) questionToUpdate).setOptions(checkboxQuestion.getOptions());
            }
        } else if (updatedQuestion instanceof FreeTextQuestion) {
            // nothing to update
        } else if (updatedQuestion instanceof TeamQuestion teamQuestion) {
            if (teamQuestion.getFinalTeamsOnly() != null) {
                ((TeamQuestion) questionToUpdate)
                        .setFinalTeamsOnly(teamQuestion.getFinalTeamsOnly());
            }
            if (teamQuestion.getTeams() != null) {
                ((TeamQuestion) questionToUpdate).setTeams(teamQuestion.getTeams());
            }

        } else if (updatedQuestion instanceof TeamOneFreeTextQuestion) {
            // nothing to update
        } else {
            throw new IllegalArgumentException("Question type not supported.");
        }

        Question savedQuestion = questionRepository.save(questionToUpdate);

        webSocketService.sendMessage("/topic/questions", "update");
        if (questionCanBeAnswered) {
            adminNotificationCreateService.sendNotificationToAll("Neue Umfrage verfügbar!",
                    questionToUpdate.getQuestionText());
        }
        return questionReturnDTOService.questionToQuestionReturnDTO(savedQuestion);
    }
}
